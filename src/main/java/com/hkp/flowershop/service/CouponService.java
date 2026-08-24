package com.hkp.flowershop.service;

import com.hkp.flowershop.dto.requests.CreateCouponRequest;
import com.hkp.flowershop.dto.requests.UpdateCouponRequest;
import com.hkp.flowershop.exceptions.BadRequestException;
import com.hkp.flowershop.exceptions.ResourceNotFoundException;
import com.hkp.flowershop.model.Coupon;
import com.hkp.flowershop.repository.CouponRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepo couponRepo;

    public List<Coupon> getAllCoupons() {
        return couponRepo.findAll();
    }

    public Coupon findById(Long id) {
        return couponRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));
    }

    @Transactional
    public Coupon createCoupon(CreateCouponRequest request) {
        String code = normalizeCode(request.getCode());
        if (couponRepo.existsByCodeIgnoreCase(code)) {
            throw new BadRequestException("Coupon code '" + code + "' already exists");
        }

        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setAmount(request.getAmount());
        coupon.setActive(request.getActive() == null || request.getActive());
        coupon.setExpiresAt(request.getExpiresAt());
        return couponRepo.save(coupon);
    }

    @Transactional
    public Coupon updateCoupon(Long id, UpdateCouponRequest request) {
        Coupon coupon = findById(id);
        if (request.getAmount() != null) {
            coupon.setAmount(request.getAmount());
        }
        if (request.getActive() != null) {
            coupon.setActive(request.getActive());
        }
        if (request.getExpiresAt() != null) {
            coupon.setExpiresAt(request.getExpiresAt());
        }
        return couponRepo.save(coupon);
    }

    @Transactional
    public void deleteCoupon(Long id) {
        Coupon coupon = findById(id);
        couponRepo.delete(coupon);
    }

    // Validates a code against an order subtotal and returns the coupon to apply
    @Transactional
    public Coupon validateForOrder(String rawCode, double subtotal) {
        String code = normalizeCode(rawCode);

        Coupon coupon = couponRepo.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new BadRequestException("Invalid coupon code"));

        if (!coupon.isActive()) {
            throw new BadRequestException("Coupon '" + code + "' is no longer active");
        }
        if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Coupon '" + code + "' has expired");
        }
        if (subtotal <= 0) {
            throw new BadRequestException("Order subtotal must be greater than 0");
        }
        return coupon;
    }

    // Fixed amount off, capped so the total never goes below zero
    public double calculateDiscount(Coupon coupon, double subtotal) {
        return Math.min(coupon.getAmount(), subtotal);
    }

    @Transactional
    public void markUsed(Coupon coupon) {
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepo.save(coupon);
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BadRequestException("Coupon code is required");
        }
        return code.trim().toUpperCase();
    }
}
