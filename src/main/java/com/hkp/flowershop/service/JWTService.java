package com.hkp.flowershop.service;

import com.hkp.flowershop.model.User;
import com.hkp.flowershop.model.UserPrinciple;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class JWTService {


    @Value("${app.jwt.secret}")
    private  String SECRET_KEY;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    public String generateToken(UserPrinciple userPrinciple) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "access");
        claims.put("role", userPrinciple.getRole());

        return Jwts.builder()
                .claims(claims)
                .subject(userPrinciple.getUsername())
                .issuer("FlowerShop")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // 1 hour
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateRefreshToken() {
        return java.util.UUID.randomUUID().toString();
    }

}
