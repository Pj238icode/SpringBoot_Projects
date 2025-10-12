package com.boot.backend.ContactManager.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    public Map<String, Object> createPaymentIntent(Long amount, String currency,String username) throws StripeException {
        Stripe.apiKey =stripeSecretKey;

        Map<String, String> metadata = new HashMap<>();
        metadata.put("email", username);

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(amount)
                        .setCurrency(currency)
                        .putAllMetadata(metadata)
                        .build();

        PaymentIntent intent = PaymentIntent.create(params);

        Map<String, Object> response = new HashMap<>();
        response.put("clientSecret", intent.getClientSecret());
        response.put("status", intent.getStatus());

        return response;
    }
}
