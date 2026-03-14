package com.pm.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication; // Missing Import
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pm.auth.dto.UserDTO;
import com.pm.auth.entity.User;
import com.pm.auth.entity.Role;
import com.pm.auth.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService; // Ensure casing matches your file name
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                       JWTService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public String generateToken(String email, String password) {
        // This will use the AuthenticationProvider we configured to check the DB
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        if (authenticate.isAuthenticated()) {
            return jwtService.generateToken(email);
        } else {
            throw new RuntimeException("Invalid access");
        }
    }

    public User registerUser(UserDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email is already registered!");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        
        if (dto.getRole() != null) {
            user.setRole(Role.valueOf(dto.getRole().toUpperCase()));
        } else {
            user.setRole(Role.CUSTOMER);
        }
        
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        
        return userRepository.save(user);
    }
    
    public void validateToken(String token) {
        jwtService.validateToken(token);
    }
}