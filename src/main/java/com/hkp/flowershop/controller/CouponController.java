package com.hkp.flowershop.controller;

import com.hkp.flowershop.dto.CouponDto;
import com.hkp.flowershop.dto.requests.ApplyCouponRequest;
import com.hkp.flowershop.dto.requests.CreateCouponRequest;
import com.hkp.flowershop.dto.requests.UpdateCouponRequest;
import com.hkp.flowershop.exceptions.BadRequestException;
import com.hkp.flowershop.exceptions.ResourceNotFoundException;
import com.hkp.flowershop.mapper.CouponMapper;
import com.hkp.flowershop.model.Coupon;
import com.hkp.flowershop.service.CouponService;
import com.hkp.flowershop.service.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponMapper couponMapper;

    @GetMapping
    public ResponseEntity<?> getAllCoupons() {
        try {
            List<CouponDto> coupons = couponService.getAllCoupons().stream()
                    .map(couponMapper::toDto)
                    .toList();
            return ResponseUtil.success(coupons);
        } catch (Exception e) {
            log.error("Error while fetching coupons", e);
            return ResponseUtil.internalError("Internal Server Error");
        }
    }

    // Lets a customer check a code before placing the order; returns the applied discount
    @PostMapping("/apply")
    public ResponseEntity<?> applyCoupon(@Valid @RequestBody ApplyCouponRequest request) {
        try {
            Coupon coupon = couponService.validateForOrder(request.getCode(), request.getSubtotal());
            double discount = couponService.calculateDiscount(coupon, request.getSubtotal());

            Map<String, Object> result = new HashMap<>();
            result.put("code", coupon.getCode());
            result.put("discountAmount", discount);
            return ResponseUtil.success(result, "Coupon applied");
        } catch (BadRequestException e) {
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error while applying coupon", e);
            return ResponseUtil.internalError("Internal Server Error");
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        try {
            Coupon createdCoupon = couponService.createCoupon(request);
            return ResponseUtil.created(couponMapper.toDto(createdCoupon), "Coupon created successfully");
        } catch (BadRequestException e) {
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error while creating coupon", e);
            return ResponseUtil.internalError("Internal Server Error");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateCoupon(@PathVariable Long id, @Valid @RequestBody UpdateCouponRequest request) {
        try {
            Coupon updatedCoupon = couponService.updateCoupon(id, request);
            return ResponseUtil.success(couponMapper.toDto(updatedCoupon), "Coupon updated successfully");
        } catch (ResourceNotFoundException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("Error while updating coupon", e);
            return ResponseUtil.internalError("Internal Server Error");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteCoupon(@PathVariable Long id) {
        try {
            couponService.deleteCoupon(id);
            return ResponseUtil.success("Coupon deleted successfully with Id " + id);
        } catch (ResourceNotFoundException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("Error while deleting coupon", e);
            return ResponseUtil.internalError("Internal Server Error");
        }
    }
}
