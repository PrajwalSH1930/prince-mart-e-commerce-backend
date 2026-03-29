package com.pm.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.auth.client.AuditClient;
import com.pm.auth.dto.AuditLogRequest;
import com.pm.auth.dto.UserDTO;
import com.pm.auth.entity.Addresses;
import com.pm.auth.entity.Role;
import com.pm.auth.entity.User;
import com.pm.auth.exception.ResourceNotFoundException;
import com.pm.auth.repository.AddressRepository;
import com.pm.auth.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AddressRepository addressRepository;
    private final AuditClient auditClient;
    private final ObjectMapper objectMapper;

    public AuthService(UserRepository userRepository, 
                       PasswordEncoder passwordEncoder, 
                       JWTService jwtService, 
                       AuthenticationManager authenticationManager, 
                       AddressRepository addressRepository,
                       AuditClient auditClient,
                       ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.addressRepository = addressRepository;
        this.auditClient = auditClient;
        this.objectMapper = objectMapper;
    }

    public String generateToken(String email, String password) {
        try {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            if (authenticate.isAuthenticated()) {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
                // Audit successful login
                sendAuditLog(user.getUserId(), "USER_LOGIN", "Email: " + email, "SUCCESS");
                
                return jwtService.generateToken(email, user.getUserId());
            } else {
                throw new RuntimeException("Invalid Access");
            }
        } catch (Exception e) {
            // Audit failed login attempt
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

        // Security: Create a copy for logging that doesn't include the password hash
        User logUser = new User();
        logUser.setUserId(savedUser.getUserId());
        logUser.setEmail(savedUser.getEmail());
        logUser.setRole(savedUser.getRole());

        sendAuditLog(savedUser.getUserId(), "USER_REGISTRATION", null, logUser);

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

        // Audit adding a new shipping address
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

    // Centralized Helper Method
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
}