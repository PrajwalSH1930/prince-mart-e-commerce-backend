package com.pm.auth.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pm.auth.dto.AddressDTO;
import com.pm.auth.dto.AuthRequest;
import com.pm.auth.dto.UserDTO;
import com.pm.auth.dto.UserResponse; 
import com.pm.auth.entity.Addresses;
import com.pm.auth.entity.User;
import com.pm.auth.entity.UserProfile;
import com.pm.auth.repository.UserRepository; 
import com.pm.auth.repository.UserProfileRepository; 
import com.pm.auth.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository; 
    private final UserProfileRepository userProfileRepository; 

    public AuthController(AuthService authService, 
                          UserRepository userRepository, 
                          UserProfileRepository userProfileRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody UserDTO dto) {
        return new ResponseEntity<>(authService.registerUser(dto), HttpStatus.CREATED);
    }
    
    @PostMapping("/token")
    public String getToken(@RequestBody AuthRequest authRequest) {
        return authService.generateToken(authRequest.getEmail(), authRequest.getPassword());
    }

    @GetMapping("/validate")
    public String validateToken(@RequestParam("token") String token) {
        authService.validateToken(token); 
        return authService.extractUserId(token);
    }
    
    @GetMapping("/id/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserProfile profile = userProfileRepository.findByUser(user)
                .orElse(new UserProfile()); 

        UserResponse response = new UserResponse();
        response.setId(user.getUserId());
        response.setEmail(user.getEmail());
        
        String fName = profile.getFirstName() != null ? profile.getFirstName() : "";
        String lName = profile.getLastName() != null ? profile.getLastName() : "";
        String fullName = (fName + " " + lName).trim();
        response.setFullName(fullName.isEmpty() ? "Prince Mart User" : fullName); 
        
        return ResponseEntity.ok(response);
    }

    // --- NEW: Profile Endpoints ---
    @PostMapping("/profile")
    public ResponseEntity<UserProfile> saveProfile(
            @RequestHeader("X-User-Id") Long userId, 
            @RequestBody UserProfile profile) {
        return ResponseEntity.ok(authService.saveOrUpdateProfile(userId, profile));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfile> getMyProfile(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(authService.getProfileByUserId(userId));
    }
    
    @PostMapping("/addresses/add")
    public ResponseEntity<Addresses> addAddress(
            @RequestHeader("X-User-Id") Long userId, 
            @RequestBody Addresses address) {
        return ResponseEntity.ok(authService.addAddress(userId, address));
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<Addresses>> getMyAddresses(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(authService.getUserAddresses(userId));
    }
    
    @GetMapping("/addresses/id/{id}")
    public ResponseEntity<Addresses> getAddressById(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getAddressById(id));
    }
    
    @DeleteMapping("/addresses/id/{addressId}")
    public ResponseEntity<Void> deleteAddressById(@PathVariable Long addressId, @RequestHeader("X-User-Id") Long userId) {
		authService.deleteAddress(userId, addressId);
		return ResponseEntity.noContent().build();
	}
    
 // Inside AddressController.java
    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<Addresses> updateAddress(
        @PathVariable Long addressId, 
        @RequestBody AddressDTO addressDetails,
        @RequestHeader("X-User-Id") Long userId) {
        
        // Logic: Find address, verify it belongs to this userId, then update fields
        Addresses updatedAddress = authService.updateAddress(addressId, userId, addressDetails);
        return ResponseEntity.ok(updatedAddress);
    }
    
    @GetMapping("/all-users")
    public ResponseEntity<List<User>> getAllUsers() {
		return ResponseEntity.ok(userRepository.findAll());
	}
    
    @GetMapping("/userprofile/{userId}")
    public ResponseEntity<UserProfile> getUserProfileByUserId(@PathVariable Long userId) {
		return ResponseEntity.ok(authService.getProfileByUserId(userId));
	}
    
    @GetMapping("/addresses/user/{userId}")
    public ResponseEntity<List<Addresses>> getAddressesByUserId(@PathVariable Long userId) {
    			return ResponseEntity.ok(authService.getUserAddresses(userId));
    }
}