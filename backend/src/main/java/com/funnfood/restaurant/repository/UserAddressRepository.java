package com.funnfood.restaurant.repository;

import com.funnfood.restaurant.model.UserAddress;
import com.funnfood.restaurant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUser(User user);

    Optional<UserAddress> findByUserAndIsDefault(User user, boolean isDefault);
}
