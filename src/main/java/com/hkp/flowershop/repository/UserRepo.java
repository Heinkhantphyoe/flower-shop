package com.hkp.flowershop.repository;

import com.hkp.flowershop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    public User findByName(String username);

    public User findByEmail(String email);

    boolean existsByEmail(String email);
}
