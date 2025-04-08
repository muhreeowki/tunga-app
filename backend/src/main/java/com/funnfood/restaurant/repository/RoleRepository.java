package com.funnfood.restaurant.repository;

import com.funnfood.restaurant.model.ERole;
import com.funnfood.restaurant.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
