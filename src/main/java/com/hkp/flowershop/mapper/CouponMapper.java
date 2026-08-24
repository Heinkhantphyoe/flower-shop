package com.hkp.flowershop.mapper;

import com.hkp.flowershop.dto.CouponDto;
import com.hkp.flowershop.model.Coupon;
import org.springframework.stereotype.Component;

@Component
public class CouponMapper {

    public CouponDto toDto(Coupon coupon) {
        CouponDto dto = new CouponDto();
        dto.setId(coupon.getId());
        dto.setCode(coupon.getCode());
        dto.setAmount(coupon.getAmount());
        dto.setActive(coupon.isActive());
        dto.setExpiresAt(coupon.getExpiresAt());
        dto.setUsedCount(coupon.getUsedCount());
        return dto;
    }
}
