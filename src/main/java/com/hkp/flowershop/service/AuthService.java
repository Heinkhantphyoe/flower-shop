package com.hkp.flowershop.service;

import com.hkp.flowershop.dto.response.LoginResponse;
import com.hkp.flowershop.model.User;
import com.hkp.flowershop.model.UserPrinciple;
import com.hkp.flowershop.repository.UserRepo;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    UserRepo repo;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JWTService jwtService;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public User registerUser(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        return repo.save(user);
    }

    public LoginResponse verify(String email, String password) {
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        UserPrinciple userPrinciple = (UserPrinciple) authentication.getPrincipal();

                if (authentication.isAuthenticated()){
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    String jwtToken =  jwtService.generateToken(userPrinciple);
                    return LoginResponse.builder()
                            .role(userPrinciple.getRole().name())
                            .token(jwtToken)
                            .build();
                }

                throw new BadCredentialsException("Invalid email or password");

    }
}
