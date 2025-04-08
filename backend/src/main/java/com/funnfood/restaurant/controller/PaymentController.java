package com.funnfood.restaurant.controller;

import com.funnfood.restaurant.payload.request.StripePaymentRequest;
import com.funnfood.restaurant.payload.response.MessageResponse;
import com.funnfood.restaurant.payload.response.StripePaymentResponse;
import com.funnfood.restaurant.security.services.UserDetailsImpl;
import com.funnfood.restaurant.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/stripe/create-payment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StripePaymentResponse> createStripePayment(
            @Valid @RequestBody StripePaymentRequest request,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        StripePaymentResponse response = paymentService.createStripePayment(request, userDetails.getId());
        return ResponseEntity.ok(response);


    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<MessageResponse> handleStripeWebhook(
            @RequestHeader("Stripe-Signature") String sigHeader,
            HttpServletRequest request) throws IOException {

        // Read the request body
        String payload = request.getReader().lines().collect(Collectors.joining());

        // Process the webhook
        paymentService.handleStripeWebhook(payload, sigHeader);

        return ResponseEntity.ok(new MessageResponse("Webhook processed successfully"));
    }

    @PostMapping("/stripe/update-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> updatePaymentStatus(
            @RequestParam String paymentIntentId,
            @RequestParam String status) {

        paymentService.updatePaymentStatus(paymentIntentId, status);

        return ResponseEntity.ok(new MessageResponse("Payment status updated successfully"));
    }
}
