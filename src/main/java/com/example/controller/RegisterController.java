package com.example.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.model.User;
import com.example.repository.UserRepository;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000") // allow React dev server
public class RegisterController {

    @Autowired
    private UserRepository uRepo;

    @PostMapping("/register")
    public Object postSignup(@RequestBody User u) throws IOException {
        // Check if email already exists
        Optional<User> existingUser = uRepo.findByEmail(u.getEmail());

        if (existingUser.isPresent()) {
            System.out.println("User Exists");
            return new ApiResponse(false, "Email already signed up");
        }
        System.out.println("reached");
        // Hash password
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        u.setPassword(encoder.encode(u.getPassword()));

        // Save user
        User savedUser = uRepo.save(u);
        if (savedUser != null) {
            return new ApiResponse(true, "User registered successfully");
        } else {
            return new ApiResponse(false, "User registration failed");
        }
    }

    // Small helper class for JSON responses
    static class ApiResponse {
        public boolean success;
        public String message;

        public ApiResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
