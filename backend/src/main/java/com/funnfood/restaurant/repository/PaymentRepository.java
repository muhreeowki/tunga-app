package com.funnfood.restaurant.repository;

import com.funnfood.restaurant.model.Order;
import com.funnfood.restaurant.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder(Order order);

    Optional<Payment> findByStripePaymentIntentId(String paymentIntentId);

    Optional<Payment> findByTransactionId(String transactionId);
}
