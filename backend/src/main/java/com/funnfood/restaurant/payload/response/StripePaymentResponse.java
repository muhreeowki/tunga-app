package com.funnfood.restaurant.payload.response;

public class StripePaymentResponse {
    private String clientSecret;
    private String paymentIntentId;
    private String status;

    public StripePaymentResponse() {
    }

    public StripePaymentResponse(String clientSecret, String paymentIntentId, String status) {
        this.clientSecret = clientSecret;
        this.paymentIntentId = paymentIntentId;
        this.status = status;
    }

    // Getters and Setters
    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
