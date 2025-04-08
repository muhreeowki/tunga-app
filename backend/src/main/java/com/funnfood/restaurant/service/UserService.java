package com.funnfood.restaurant.service;

import com.funnfood.restaurant.exception.ResourceNotFoundException;
import com.funnfood.restaurant.model.User;
import com.funnfood.restaurant.model.VerificationToken;
import com.funnfood.restaurant.repository.UserRepository;
import com.funnfood.restaurant.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private EmailService emailService;

    @Value("${app.verification-token.expiration-minutes}")
    private int tokenExpirationMinutes;

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    @Transactional
    public void sendVerificationEmail(User user, String baseUrl) {
        // Delete existing token if any
        verificationTokenRepository.findByUser(user).ifPresent(token -> {
            verificationTokenRepository.delete(token);
        });

        // Create new verification token
        VerificationToken verificationToken = new VerificationToken(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(tokenExpirationMinutes));
        verificationTokenRepository.save(verificationToken);

        // Send email with verification link
        String verificationUrl = baseUrl + "/api/auth/verify?token=" + verificationToken.getToken();
        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), verificationUrl);
    }

    @Transactional
    public boolean verifyEmail(String token) {
        Optional<VerificationToken> verificationTokenOpt = verificationTokenRepository.findByToken(token);

        if (verificationTokenOpt.isEmpty()) {
            return false;
        }

        VerificationToken verificationToken = verificationTokenOpt.get();

        if (verificationToken.isExpired()) {
            verificationTokenRepository.delete(verificationToken);
            return false;
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);

        return true;
    }

    @Transactional
    public boolean resendVerificationEmail(String email, String baseUrl) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();

        if (user.isEmailVerified()) {
            return false;
        }

        sendVerificationEmail(user, baseUrl);
        return true;
    }
}
