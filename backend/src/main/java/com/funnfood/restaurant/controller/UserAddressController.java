package com.funnfood.restaurant.controller;

import com.funnfood.restaurant.model.User;
import com.funnfood.restaurant.model.UserAddress;
import com.funnfood.restaurant.service.UserAddressService;
import com.funnfood.restaurant.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/addresses")
public class UserAddressController {
    @Autowired
    private UserAddressService userAddressService;

    @Autowired
    private UserService userService;

    // Get all addresses for current user
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<UserAddress>> getMyAddresses() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userService.getUserByUsername(username);

        List<UserAddress> addresses = userAddressService.getAddressesByUser(currentUser);
        return ResponseEntity.ok(addresses);
    }

    // Get address by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getAddressById(@PathVariable Long id) {
        UserAddress address = userAddressService.getAddressById(id);

        // Check if the current user has permission to view this address
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        if (address.getUser().getUsername().equals(currentUsername) ||
                auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.ok(address);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // Create new address
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<UserAddress> createAddress(@Valid @RequestBody UserAddress address) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userService.getUserByUsername(username);

        address.setUser(currentUser);
        UserAddress newAddress = userAddressService.createAddress(address);
        return new ResponseEntity<>(newAddress, HttpStatus.CREATED);
    }

    // Update address
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateAddress(@PathVariable Long id, @Valid @RequestBody UserAddress address) {
        UserAddress existingAddress = userAddressService.getAddressById(id);

        // Check if the current user has permission to update this address
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        if (existingAddress.getUser().getUsername().equals(currentUsername) ||
                auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {

            UserAddress updatedAddress = userAddressService.updateAddress(id, address);
            return ResponseEntity.ok(updatedAddress);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // Delete address
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteAddress(@PathVariable Long id) {
        UserAddress address = userAddressService.getAddressById(id);

        // Check if the current user has permission to delete this address
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        if (address.getUser().getUsername().equals(currentUsername) ||
                auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {

            userAddressService.deleteAddress(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // Set address as default
    @PatchMapping("/{id}/default")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> setDefaultAddress(@PathVariable Long id) {
        UserAddress address = userAddressService.getAddressById(id);

        // Check if the current user has permission to update this address
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        if (address.getUser().getUsername().equals(currentUsername) ||
                auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {

            UserAddress defaultAddress = userAddressService.setDefaultAddress(id);
            return ResponseEntity.ok(defaultAddress);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
