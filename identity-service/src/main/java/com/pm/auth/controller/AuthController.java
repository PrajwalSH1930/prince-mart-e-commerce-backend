package com.pm.auth.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pm.auth.dto.AuthRequest;
import com.pm.auth.dto.UserDTO;
import com.pm.auth.dto.UserResponse; // Ensure you created this DTO
import com.pm.auth.entity.Addresses;
import com.pm.auth.entity.User;
import com.pm.auth.entity.UserProfile;
import com.pm.auth.repository.UserRepository; // Added
import com.pm.auth.repository.UserProfileRepository; // Added
import com.pm.auth.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository; // Added
    private final UserProfileRepository userProfileRepository; // Added

    // Updated Constructor
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
    
    // Fixed: Matches the {id} path with the userId variable
    @GetMapping("/id/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long userId) {
        // 1. Fetch User (for email)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("Fetched User Email: " + user.getEmail()); // Debug log to check the fetched email
        // 2. Fetch UserProfile (for name) using the user object
        // This effectively "joins" the data from the user_profiles table
        UserProfile profile = userProfileRepository.findByUser(user)
                .orElse(new UserProfile()); 

        // 3. Map to DTO
        UserResponse response = new UserResponse();
        response.setId(user.getUserId());
        response.setEmail(user.getEmail());
        
        // Combine FirstName and LastName safely
        String fName = profile.getFirstName() != null ? profile.getFirstName() : "";
        String lName = profile.getLastName() != null ? profile.getLastName() : "";
        System.out.println("Fetched First Name: " + fName); // Debug log to check the fetched name
        String fullName = (fName + " " + lName).trim();
        response.setFullName(fullName.isEmpty() ? "Prince Mart User" : fullName); // Default to "Prince User" if no name is found
        
        return ResponseEntity.ok(response);
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
}