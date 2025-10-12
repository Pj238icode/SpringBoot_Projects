package com.boot.backend.ContactManager.controllers;


import com.boot.backend.ContactManager.entities.User;
import com.boot.backend.ContactManager.repositories.UserRespository;
import com.boot.backend.ContactManager.requests.PaymentRequest;
import com.boot.backend.ContactManager.services.PaymentService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRespository userRepository;

    @PostMapping("/create-intent")
    public Map<String, Object> createPaymentIntent(
            @RequestBody PaymentRequest paymentRequest,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws StripeException {

        // Extract from request body
        Long amount = paymentRequest.getAmount();
        String currency = paymentRequest.getCurrency();

        // Access logged-in user info
        String username = userDetails.getUsername();
        User user = userRepository.findByEmail(username);



        // Create PaymentIntent
        return paymentService.createPaymentIntent(amount, currency,username);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader("Stripe-Signature") String sigHeader) {
        String endpointSecret = "YOUR_STRIPE_WEBHOOK_SECRET"; // get from Stripe dashboard
        try {
            // Verify the event with Stripe library
            com.stripe.model.Event event = com.stripe.net.Webhook.constructEvent(
                    payload, sigHeader, endpointSecret
            );

            switch (event.getType()) {
                case "payment_intent.succeeded":
                    com.stripe.model.PaymentIntent paymentIntent =
                            (com.stripe.model.PaymentIntent) event.getDataObjectDeserializer()
                                    .getObject().orElse(null);
                    if (paymentIntent != null) {
                        Long amountReceived = paymentIntent.getAmountReceived(); // in smallest currency unit (paise for INR)

                        // Example: fetch user email from metadata if you added it while creating PaymentIntent
                        String userEmail = paymentIntent.getMetadata().get("email");
                        if (userEmail != null) {
                            User user = userRepository.findByEmail(userEmail);
                            if (user != null) {
                                // Set credits based on amount received
                                if (amountReceived == 20000) {
                                    user.setCredits(200L);
                                } else if (amountReceived == 40000) {
                                    user.setCredits(400L);
                                }
                                userRepository.save(user);
                                System.out.println("Updated credits for user: " + userEmail);
                            }
                        }
                    }
                    break;


                case "payment_intent.payment_failed":
                    System.out.println("Payment failed!");
                    break;

                default:
                    System.out.println("Unhandled event type: " + event.getType());
            }

            return ResponseEntity.ok("Webhook received");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Webhook error: " + e.getMessage());
        }
    }



}
