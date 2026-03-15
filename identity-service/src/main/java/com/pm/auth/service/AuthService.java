package com.pm.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.pm.auth.dto.UserDTO;
import com.pm.auth.entity.User;
import com.pm.auth.exception.ResourceNotFoundException;
import com.pm.auth.entity.Role;
import com.pm.auth.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                       JWTService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public String generateToken(String email, String password) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        if (authenticate.isAuthenticated()) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            return jwtService.generateToken(email, user.getUserId());
        } else {
            throw new RuntimeException("Invalid Access");
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
        
        return userRepository.save(user);
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
}