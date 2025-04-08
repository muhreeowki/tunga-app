package com.funnfood.restaurant.service;

import com.funnfood.restaurant.exception.BadRequestException;
import com.funnfood.restaurant.exception.ResourceNotFoundException;
import com.funnfood.restaurant.model.ERole;
import com.funnfood.restaurant.model.Role;
import com.funnfood.restaurant.model.User;
import com.funnfood.restaurant.model.VerificationToken;
import com.funnfood.restaurant.payload.request.LoginRequest;
import com.funnfood.restaurant.payload.request.SignupRequest;
import com.funnfood.restaurant.payload.response.JwtResponse;
import com.funnfood.restaurant.payload.response.MessageResponse;
import com.funnfood.restaurant.repository.RoleRepository;
import com.funnfood.restaurant.repository.UserRepository;
import com.funnfood.restaurant.repository.VerificationTokenRepository;
import com.funnfood.restaurant.security.jwt.JwtUtils;
import com.funnfood.restaurant.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private EmailService emailService;

    @Value("${app.verification-token.expiration-minutes}")
    private int tokenExpirationMinutes;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.isEmailVerified(),
                roles
        );
    }

    @Transactional
    public MessageResponse registerUser(SignupRequest signupRequest, String baseUrl) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        // Create new user's account
        User user = new User(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                encoder.encode(signupRequest.getPassword())
        );

        Set<String> strRoles = signupRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role ROLE_USER not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_ADMIN not found."));
                        roles.add(adminRole);
                        break;
                    case "manager":
                        Role modRole = roleRepository.findByName(ERole.ROLE_RESTAURANT_MANAGER)
                                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_MANAGER not found."));
                        roles.add(modRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_USER not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        // Create verification token and send email
        VerificationToken verificationToken = new VerificationToken(user);
        verificationTokenRepository.save(verificationToken);

        String verificationUrl = baseUrl + "/api/auth/verify?token=" + verificationToken.getToken();
        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), verificationUrl);

        return new MessageResponse("User registered successfully! Please check your email to verify your account.");
    }

    @Transactional
    public MessageResponse verifyEmail(String token) {
        Optional<VerificationToken> verificationTokenOpt = verificationTokenRepository.findByToken(token);

        if (verificationTokenOpt.isEmpty()) {
            throw new ResourceNotFoundException("Verification token", "token", token);
        }

        VerificationToken verificationToken = verificationTokenOpt.get();

        if (verificationToken.isExpired()) {
            verificationTokenRepository.delete(verificationToken);
            throw new BadRequestException("Token has expired, please request a new one");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);

        return new MessageResponse("Email verified successfully!");
    }

    @Transactional
    public MessageResponse resendVerificationToken(String email, String baseUrl) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User", "email", email);
        }

        User user = userOpt.get();

        if (user.isEmailVerified()) {
            return new MessageResponse("Email is already verified!");
        }

        // Delete existing token if any
        Optional<VerificationToken> existingToken = verificationTokenRepository.findByUser(user);
        if (existingToken.isPresent()) {
            verificationTokenRepository.delete(existingToken.get());
        }

        // Create new token
        VerificationToken verificationToken = new VerificationToken(user);
        verificationTokenRepository.save(verificationToken);

        // Send verification email
        String verificationUrl = baseUrl + "/api/auth/verify?token=" + verificationToken.getToken();
        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), verificationUrl);

        return new MessageResponse("Verification email resent successfully!");
    }
}
