package com.boot.backend.food_delivery_app.controller;

import com.boot.backend.food_delivery_app.io.UserRequest;
import com.boot.backend.food_delivery_app.io.UserResponse;
import com.boot.backend.food_delivery_app.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;


    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody UserRequest request) {
        return userService.registerUser(request);
    }
}
