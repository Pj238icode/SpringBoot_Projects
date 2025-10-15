package com.boot.backend.food_delivery_app.service;

import com.boot.backend.food_delivery_app.io.UserRequest;
import com.boot.backend.food_delivery_app.io.UserResponse;

public interface UserService {

    UserResponse registerUser(UserRequest request);

    String findByUserId();
}
