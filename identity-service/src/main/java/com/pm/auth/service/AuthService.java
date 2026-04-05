package com.pm.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.auth.client.AuditClient;
import com.pm.auth.client.NotificationClient;
import com.pm.auth.dto.AddressDTO;
import com.pm.auth.dto.AuditLogRequest;
import com.pm.auth.dto.NotificationRequest;
import com.pm.auth.dto.UserDTO;
import com.pm.auth.entity.Addresses;
import com.pm.auth.entity.Role;
import com.pm.auth.entity.User;
import com.pm.auth.entity.UserProfile;
import com.pm.auth.exception.ResourceNotFoundException;
import com.pm.auth.repository.AddressRepository;
import com.pm.auth.repository.UserProfileRepository;
import com.pm.auth.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AddressRepository addressRepository;
    private final AuditClient auditClient;
    private final ObjectMapper objectMapper;
    private final NotificationClient notificationClient;

    public AuthService(UserRepository userRepository, 
                       PasswordEncoder passwordEncoder, 
                       JWTService jwtService, 
                       AuthenticationManager authenticationManager, 
                       AddressRepository addressRepository,
                       AuditClient auditClient,
                       ObjectMapper objectMapper, 
                       NotificationClient notificationClient, 
                       UserProfileRepository userProfileRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.addressRepository = addressRepository;
        this.auditClient = auditClient;
        this.objectMapper = objectMapper;
        this.notificationClient = notificationClient;
    }

    public String generateToken(String email, String password) {
        try {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            if (authenticate.isAuthenticated()) {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
                sendAuditLog(user.getUserId(), "USER_LOGIN", "Email: " + email, "SUCCESS");
                
                return jwtService.generateToken(email, user.getUserId());
            } else {
                throw new RuntimeException("Invalid Access");
            }
        } catch (Exception e) {
            sendAuditLog(null, "LOGIN_FAILURE", "Attempted Email: " + email, "FAILED: " + e.getMessage());
            throw e;
        }
    }

    public User registerUser(UserDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ResourceNotFoundException("Email is already registered!");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole() != null ? Role.valueOf(dto.getRole().toUpperCase()) : Role.CUSTOMER);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        
        User savedUser = userRepository.save(user);

        User logUser = new User();
        logUser.setUserId(savedUser.getUserId());
        logUser.setEmail(savedUser.getEmail());
        logUser.setRole(savedUser.getRole());
        sendAuditLog(savedUser.getUserId(), "USER_REGISTRATION", null, logUser);

        try {
            NotificationRequest welcomeMail = new NotificationRequest(
                savedUser.getEmail(), 
                dto.getEmail(), 
                "WELCOME"
            );
            notificationClient.sendNotification(welcomeMail);
        } catch (Exception e) {
            System.err.println("Notification trigger failed: " + e.getMessage());
        }

        return savedUser;
    }

    public void validateToken(String token) {
        jwtService.validateToken(token); 
    }
    
    public String extractUserId(String token) {
        return jwtService.extractUserId(token);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User Not Found!!"));
    }
    
    public Addresses addAddress(Long userId, Addresses address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        address.setUser(user);
        Addresses savedAddress = addressRepository.save(address);

        sendAuditLog(userId, "ADD_ADDRESS", null, savedAddress);

        return savedAddress;
    }

    public List<Addresses> getUserAddresses(Long userId) {
        return addressRepository.findByUser_UserId(userId);
    }
    
    public Addresses getAddressById(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
    }

    // --- NEW: User Profile Logic ---
    public UserProfile saveOrUpdateProfile(Long userId, UserProfile profileData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserProfile profile = userProfileRepository.findByUser(user)
                .map(existing -> {
                    existing.setFirstName(profileData.getFirstName());
                    existing.setLastName(profileData.getLastName());
                    existing.setGender(profileData.getGender());
                    existing.setDateOfBirth(profileData.getDateOfBirth());
                    existing.setProfileImage(profileData.getProfileImage());
                    return existing;
                }).orElseGet(() -> {
                    profileData.setUser(user);
                    return profileData;
                });

        UserProfile savedProfile = userProfileRepository.save(profile);
        sendAuditLog(userId, "UPDATE_PROFILE", null, savedProfile);
        return savedProfile;
    }

    public UserProfile getProfileByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }

    private void sendAuditLog(Long userId, String action, Object dataBefore, Object dataAfter) {
        try {
            String before = dataBefore != null ? (dataBefore instanceof String ? (String) dataBefore : objectMapper.writeValueAsString(dataBefore)) : null;
            String after = dataAfter != null ? (dataAfter instanceof String ? (String) dataAfter : objectMapper.writeValueAsString(dataAfter)) : null;

            auditClient.createLog(new AuditLogRequest(
                "AUTH-SERVICE", 
                action, 
                userId, 
                before, 
                after
            ));
        } catch (Exception e) {
            System.err.println("Audit logging failed in Auth Service: " + e.getMessage());
        }
    }
    
    public void deleteAddress(Long userId, Long addressId) {
		Addresses address = addressRepository.findById(addressId)
				.orElseThrow(() -> new ResourceNotFoundException("Address not found"));
		
		if (!address.getUser().getUserId().equals(userId)) {
			throw new ResourceNotFoundException("Unauthorized to delete this address");
		}

		addressRepository.delete(address);
		sendAuditLog(userId, "DELETE_ADDRESS", null, "Deleted Address ID: " + addressId);
	}

    @Transactional
    public Addresses updateAddress(Long addressId, Long userId, AddressDTO dto) {
        // 1. Fetch existing address
        Addresses existing = addressRepository.findById(addressId)
            .orElseThrow(() -> new RuntimeException("Address Registry entry not found"));

        // 2. Security: Ensure the address belongs to the user
        if (!existing.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Access Denied: Registry mismatch");
        }

        // 3. If this is being set as default, unset others first
        if (dto.isDefault()) {
            addressRepository.unsetOtherDefaults(userId, addressId);
        }

        // 4. Update fields
        existing.setFullName(dto.getFullName());
        existing.setPhone(dto.getPhone());
        existing.setAddressLine1(dto.getAddressLine1());
        existing.setAddressLine2(dto.getAddressLine2());
        existing.setCity(dto.getCity());
        existing.setState(dto.getState());
        existing.setPostalCode(dto.getPostalCode());
        existing.setCountry(dto.getCountry());
        existing.setDefault(dto.isDefault());

        return addressRepository.save(existing);
    }
    
    	
}