package com.funnfood.restaurant.service;

import com.funnfood.restaurant.exception.ResourceNotFoundException;
import com.funnfood.restaurant.model.Order;
import com.funnfood.restaurant.model.Payment;
import com.funnfood.restaurant.payload.request.StripePaymentRequest;
import com.funnfood.restaurant.payload.response.StripePaymentResponse;
import com.funnfood.restaurant.repository.OrderRepository;
import com.funnfood.restaurant.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
    }

    @Transactional(readOnly = true)
    public Payment getPaymentByOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        return paymentRepository.findByOrder(order)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
    }

    @Transactional
    public Payment processPayment(Long orderId, String paymentMethod, String transactionId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount().doubleValue());
        payment.setPaymentMethod(paymentMethod);
        payment.setTransactionId(transactionId);
        payment.setStatus("COMPLETED");
        payment.setPaymentDate(LocalDateTime.now());

        // Update order status to PAID
        order.setStatus("PAID");
        orderRepository.save(order);

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment refundPayment(Long paymentId, String refundReason) {
        Payment payment = getPaymentById(paymentId);
        payment.setStatus("REFUNDED");
        payment.setRefundReason(refundReason);
        payment.setRefundDate(LocalDateTime.now());

        // Update order status to REFUNDED
        Order order = payment.getOrder();
        order.setStatus("REFUNDED");
        orderRepository.save(order);

        return paymentRepository.save(payment);
    }

    @Transactional
    public StripePaymentResponse createStripePayment(StripePaymentRequest request, Long userId) {
        // In a real implementation, this would use the Stripe API to create a PaymentIntent
        // Since we're not actually integrating with Stripe here, we'll create a placeholder response

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        // Create a placeholder for demonstration purposes
        String fakeClientSecret = "pi_" + System.currentTimeMillis() + "_secret_" + order.getId();
        String fakePaymentIntentId = "pi_" + System.currentTimeMillis();

        // Save payment record
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod("STRIPE");
        payment.setTransactionId(fakePaymentIntentId);
        payment.setStripePaymentIntentId(fakePaymentIntentId);
        payment.setStatus("PENDING");
        payment.setPaymentDate(LocalDateTime.now());

        try{
        paymentRepository.save(payment);
            paymentRepository.save(payment);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("A payment already exists for this order.");

        }

        return new StripePaymentResponse(fakeClientSecret, fakePaymentIntentId, "requires_payment_method");

    }

    @Transactional
    public void handleStripeWebhook(String payload, String signatureHeader) {
        // In a real implementation, this would validate the Stripe signature and process events
        // Here we'll just log that we received a webhook
        System.out.println("Received Stripe webhook: " + payload.substring(0, Math.min(100, payload.length())) + "...");
    }

    @Transactional
    public void updatePaymentStatus(String paymentIntentId, String status) {
        // Find payment by transactionId (paymentIntentId)
        Payment payment = paymentRepository.findByTransactionId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "transactionId", paymentIntentId));

        payment.setStatus(status);



        // If payment is successful, update order status
        if ("COMPLETED".equals(status) || "succeeded".equals(status)) {
            Order order = payment.getOrder();
            order.setStatus("PAID");
            orderRepository.save(order);
        }

        paymentRepository.save(payment);
    }
}
