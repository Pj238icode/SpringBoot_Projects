package com.boot.backend.food_delivery_app.service;

import com.boot.backend.food_delivery_app.io.CartRequest;
import com.boot.backend.food_delivery_app.io.CartResponse;

public interface CartService {

    CartResponse addToCart(CartRequest request);

    CartResponse getCart();

    void clearCart();

    CartResponse removeFromCart(CartRequest cartRequest);
}
