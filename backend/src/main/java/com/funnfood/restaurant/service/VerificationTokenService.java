package com.funnfood.restaurant.service;

import com.funnfood.restaurant.model.User;
import com.funnfood.restaurant.model.VerificationToken;
import com.funnfood.restaurant.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VerificationTokenService {

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Value("${app.verification-token.expiration-minutes}")
    private int expirationMinutes;

    @Transactional
    public VerificationToken createVerificationToken(User user) {
        // First check if a token already exists for this user
        VerificationToken existingToken = tokenRepository.findByUser(user).orElse(null);

        if (existingToken != null) {
            // If token exists but is expired, update it
            if (existingToken.isExpired()) {
                existingToken.setToken(java.util.UUID.randomUUID().toString());
                existingToken.setExpiryDate(LocalDateTime.now().plusMinutes(expirationMinutes));
                return tokenRepository.save(existingToken);
            }
            return existingToken;
        }

        // Otherwise create a new token
        VerificationToken verificationToken = new VerificationToken(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(expirationMinutes));
        return tokenRepository.save(verificationToken);
    }

    @Transactional(readOnly = true)
    public VerificationToken getVerificationToken(String token) {
        return tokenRepository.findByToken(token).orElse(null);
    }

    @Transactional
    public void deleteVerificationToken(Long id) {
        tokenRepository.deleteById(id);
    }
}
