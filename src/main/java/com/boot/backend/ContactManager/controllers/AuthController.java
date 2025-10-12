package com.boot.backend.ContactManager.controllers;


import com.boot.backend.ContactManager.Responses.ApiResponse;
import com.boot.backend.ContactManager.Responses.GoogleResponse;
import com.boot.backend.ContactManager.Responses.JwtResponse;
import com.boot.backend.ContactManager.dtos.UserDto;
import com.boot.backend.ContactManager.entities.User;
import com.boot.backend.ContactManager.exceptions.UserException;
import com.boot.backend.ContactManager.jwt.JwtHelper;
import com.boot.backend.ContactManager.repositories.UserRespository;
import com.boot.backend.ContactManager.requests.JwtRequest;
import com.boot.backend.ContactManager.services.GoogleService;
import com.boot.backend.ContactManager.services.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService  userDetailsService;

    @Autowired
    private UserRespository userRespository;

    @Autowired
    private GoogleService googleService;



    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDto userDto, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            // Wrap in ApiResponse
            return new ResponseEntity<>(new ApiResponse<>(false,"Please fix the errors",errors), HttpStatus.BAD_REQUEST);

        }

        try {
            UserDto user = userService.createUser(userDto);
            return new ResponseEntity<>(
                    new ApiResponse<UserDto>(true, "User Registered Successfully", user),
                    HttpStatus.OK
            );
        } catch (UserException e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, e.getMessage(), null),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody JwtRequest jwtRequest, BindingResult result) {
        // Step 1: Validate request fields
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return new ResponseEntity<>(new ApiResponse<>(false,"Please fix the errors",errors), HttpStatus.BAD_REQUEST);
        }

        try {
            // Step 2: Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(jwtRequest.getEmail(), jwtRequest.getPassword())
            );

            // Step 3: Load user details and generate token
            UserDetails userDetails = userDetailsService.loadUserByUsername(jwtRequest.getEmail());
            String token = jwtHelper.generateToken(jwtRequest.getEmail());
            User user = userRespository.findByEmail(jwtRequest.getEmail());
            user.setProvider("Normal");

            // Step 4: Generate avatar if not present
            if (user.getPicture() == null || user.getPicture().isEmpty()) {
                // You can use the user ID or email hash for uniqueness
                String avatarUrl = "https://avatar.iran.liara.run/public/" + user.getUserId();
                user.setPicture(avatarUrl);
                userRespository.save(user); // persist the avatar URL in DB
            }

            JwtResponse jwtResponse = new JwtResponse(user.getName(), user.getEmail(), token, user.getPicture(),user.getCredits());

            return new ResponseEntity<>(new ApiResponse<>(true, "User Logged In Successfully!", jwtResponse), HttpStatus.OK);

        } catch (DisabledException e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "User Account is Disabled!", null), HttpStatus.UNAUTHORIZED);

        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "Invalid email or password!", null), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
        try {
            String token = body.get("token"); // ID token from frontend
            GoogleIdToken.Payload payload = googleService.verifyToken(token);

            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");
            String image=null;

            // Check if user exists in DB
            User existingUser = userRespository.findByEmail(email);
            if (existingUser==null) {
                return new ResponseEntity<>(new ApiResponse<>(false, "User Not Registered ! ", null), HttpStatus.UNAUTHORIZED);
            }
            existingUser.setProvider("Google");

            // Generate your app JWT
            String jwt = jwtHelper.generateToken(email);
            if (existingUser.getPicture() == null || existingUser.getPicture().isEmpty()) {
                // You can use the user ID or email hash for uniqueness
                String avatarUrl = "https://avatar.iran.liara.run/public/" + existingUser.getUserId();
                existingUser.setPicture(avatarUrl);
                userRespository.save(existingUser);

            }

            GoogleResponse googleResponse = new GoogleResponse();
            googleResponse.setToken(jwt);
            googleResponse.setEmail(email);
            googleResponse.setName(name);
            googleResponse.setPicture(existingUser.getPicture());
            googleResponse.setCredits(existingUser.getCredits());


            return new ResponseEntity<>(new ApiResponse<>(true, "Google Login Successful",googleResponse), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "Invalid Token ", null), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return new ResponseEntity<>(
                        new ApiResponse<>(false, "Missing or invalid Authorization header!", null),
                        HttpStatus.BAD_REQUEST
                );
            }

            String token = authHeader.substring(7);
            String username = jwtHelper.extractUsername(token);

            if (username != null && jwtHelper.isTokenValid(token, username)) {
                return new ResponseEntity<>(
                        new ApiResponse<>(true, "Token is valid", username),
                        HttpStatus.OK
                );
            } else {
                return new ResponseEntity<>(
                        new ApiResponse<>(false, "Token is invalid", null),
                        HttpStatus.UNAUTHORIZED
                );
            }
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Invalid or expired token", null),
                    HttpStatus.UNAUTHORIZED
            );
        }
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<String>> logoutAllDevices(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse<>(false, "Unauthorized", null));
        }

        String email = userDetails.getUsername(); // email used as username
        User user = userRespository.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>(false, "User not found", null));
        }

        // Increment token version to invalidate all existing tokens
        jwtHelper.logoutAllDevices(email);

        return ResponseEntity.ok(new ApiResponse<>(true, "Logged out from all devices successfully", null));
    }








}
