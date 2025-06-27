package com.hkp.flowershop.service;

import com.hkp.flowershop.model.User;
import com.hkp.flowershop.model.UserPrinciple;
import com.hkp.flowershop.repository.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class MyUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepo.findByEmail(email);
        if(optionalUser.isEmpty()) {
            log.error("User not found");
            throw new UsernameNotFoundException("User not found");
        }
        User user = optionalUser.get();
        return new UserPrinciple(user);
    }
}
