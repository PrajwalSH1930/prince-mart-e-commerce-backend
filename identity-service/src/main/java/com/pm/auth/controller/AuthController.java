package com.pm.auth.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pm.auth.dto.AuthRequest;
import com.pm.auth.dto.UserDTO;
import com.pm.auth.entity.Addresses;
import com.pm.auth.entity.User;
import com.pm.auth.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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
        return authService.extractUserId(token); // Returns Long ID as String
    }
    
    @GetMapping("/id/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(authService.getUserById(id));
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