package com.funnfood.restaurant.controller;

import com.funnfood.restaurant.model.User;
import com.funnfood.restaurant.payload.request.EmailRequest;
import com.funnfood.restaurant.payload.response.MessageResponse;
import com.funnfood.restaurant.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class VerificationController {

    @Autowired
    private UserService userService;

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        boolean verified = userService.verifyEmail(token);

        if (!verified) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Invalid or expired verification token."));
        }

        return ResponseEntity.ok(new MessageResponse("Email verified successfully!"));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(
            @Valid @RequestBody EmailRequest emailRequest,
            HttpServletRequest request) {

        String baseUrl = getBaseUrl(request);
        boolean sent = userService.resendVerificationEmail(emailRequest.getEmail(), baseUrl);

        if (!sent) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email not found or already verified."));
        }

        return ResponseEntity.ok(new MessageResponse("Verification email sent successfully!"));
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        if ((serverPort != 80) && (serverPort != 443)) {
            url.append(":").append(serverPort);
        }

        url.append(contextPath);

        return url.toString();
    }
}
