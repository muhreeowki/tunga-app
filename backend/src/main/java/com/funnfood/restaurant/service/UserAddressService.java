package com.funnfood.restaurant.service;

import com.funnfood.restaurant.exception.ResourceNotFoundException;
import com.funnfood.restaurant.model.User;
import com.funnfood.restaurant.model.UserAddress;
import com.funnfood.restaurant.repository.UserAddressRepository;
import com.funnfood.restaurant.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserAddressService {
    @Autowired
    private UserAddressRepository userAddressRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all addresses for a user
     */
    @Transactional(readOnly = true)
    public List<UserAddress> getAddressesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return userAddressRepository.findByUser(user);
    }

    /**
     * Get all addresses for a user object
     */
    @Transactional(readOnly = true)
    public List<UserAddress> getAddressesByUser(User user) {
        return userAddressRepository.findByUser(user);
    }

    /**
     * Get address by ID
     */
    @Transactional(readOnly = true)
    public UserAddress getAddressById(Long id) {
        return userAddressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserAddress", "id", id));
    }

    /**
     * Get default address for a user
     */
    @Transactional(readOnly = true)
    public UserAddress getDefaultAddress(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return userAddressRepository.findByUserAndIsDefault(user, true)
                .orElseThrow(() -> new ResourceNotFoundException("Default Address", "userId", userId));
    }

    /**
     * Create a new address with user object
     */
    @Transactional
    public UserAddress createAddress(UserAddress address) {
        // User should already be set in the address from the controller
        User user = address.getUser();

        // If this is the first address for the user, make it default
        List<UserAddress> existingAddresses = userAddressRepository.findByUser(user);
        if (existingAddresses.isEmpty()) {
            address.setDefault(true);
        }

        // If this address is set as default, update all other addresses to not be default
        if (address.isDefault()) {
            updateDefaultAddressStatus(user, null);
        }

        return userAddressRepository.save(address);
    }

    /**
     * Create a new address with user ID
     */
    @Transactional
    public UserAddress createAddress(Long userId, UserAddress address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        address.setUser(user);

        // If this is the first address for the user, make it default
        List<UserAddress> existingAddresses = userAddressRepository.findByUser(user);
        if (existingAddresses.isEmpty()) {
            address.setDefault(true);
        }

        // If this address is set as default, update all other addresses to not be default
        if (address.isDefault()) {
            updateDefaultAddressStatus(user, null);
        }

        return userAddressRepository.save(address);
    }

    /**
     * Update an existing address
     */
    @Transactional
    public UserAddress updateAddress(Long id, UserAddress addressDetails) {
        UserAddress address = getAddressById(id);

        address.setStreet(addressDetails.getStreet());
        address.setCity(addressDetails.getCity());
        address.setState(addressDetails.getState());
        address.setZipCode(addressDetails.getZipCode());
        address.setPhoneNumber(addressDetails.getPhoneNumber());

        // If this address is being set as default, update other addresses
        if (addressDetails.isDefault() && !address.isDefault()) {
            updateDefaultAddressStatus(address.getUser(), address.getId());
            address.setDefault(true);
        }

        return userAddressRepository.save(address);
    }

    /**
     * Delete an address
     */
    @Transactional
    public void deleteAddress(Long id) {
        UserAddress address = getAddressById(id);
        User user = address.getUser();
        boolean wasDefault = address.isDefault();

        userAddressRepository.delete(address);

        // If this was a default address, set another address as default if available
        if (wasDefault) {
            List<UserAddress> remainingAddresses = userAddressRepository.findByUser(user);
            if (!remainingAddresses.isEmpty()) {
                UserAddress newDefault = remainingAddresses.get(0);
                newDefault.setDefault(true);
                userAddressRepository.save(newDefault);
            }
        }
    }

    /**
     * Set an address as the default for a user
     */
    @Transactional
    public UserAddress setDefaultAddress(Long addressId) {
        UserAddress newDefaultAddress = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("UserAddress", "id", addressId));

        User user = newDefaultAddress.getUser();

        // Update default status for all user addresses
        updateDefaultAddressStatus(user, addressId);

        // Set the selected address as default
        newDefaultAddress.setDefault(true);
        return userAddressRepository.save(newDefaultAddress);
    }

    /**
     * Set an address as the default for a specific user
     */
    @Transactional
    public UserAddress setDefaultAddress(Long userId, Long addressId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserAddress newDefaultAddress = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("UserAddress", "id", addressId));

        // Verify the address belongs to the user
        if (!newDefaultAddress.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("UserAddress", "id for user", addressId);
        }

        // Update default status for all user addresses
        updateDefaultAddressStatus(user, addressId);

        // Set the selected address as default
        newDefaultAddress.setDefault(true);
        return userAddressRepository.save(newDefaultAddress);
    }

    /**
     * Helper method to update default status for user addresses
     */
    private void updateDefaultAddressStatus(User user, Long excludeAddressId) {
        List<UserAddress> addresses = userAddressRepository.findByUser(user);
        for (UserAddress address : addresses) {
            if (excludeAddressId == null || !address.getId().equals(excludeAddressId)) {
                address.setDefault(false);
                userAddressRepository.save(address);
            }
        }
    }
}
