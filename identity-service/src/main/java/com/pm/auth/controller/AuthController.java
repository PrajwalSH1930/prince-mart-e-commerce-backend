package com.pm.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pm.auth.dto.AuthRequest;
import com.pm.auth.dto.UserDTO;
import com.pm.auth.entity.User;
import com.pm.auth.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome!! This is the Identity Service for Prince Mart by Prince Inc.";
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody UserDTO dto) {
        User user = authService.registerUser(dto);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }
    
    @PostMapping("/token")
    public String getToken(@RequestBody AuthRequest authRequest) {
        return authService.generateToken(authRequest.getEmail(), authRequest.getPassword());
    }

    @GetMapping("/validate")
    public String validateToken(@RequestParam("token") String token) {
        // We will implement token validation logic next
        return "Token is valid";
    }
    
    @GetMapping("/id/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long id) {
    	return ResponseEntity.ok(authService.getUserById(id));
    }
}