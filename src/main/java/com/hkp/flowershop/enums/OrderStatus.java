package com.hkp.flowershop.enums;

public enum OrderStatus {
    PENDING, CONFIRMED, DELIVERED, CANCELLED;

    public static OrderStatus fromCode(int code) {
        OrderStatus[] statuses = values();
        if (code < 0 || code >= statuses.length) {
            throw new IllegalArgumentException("Invalid orderStatus code: " + code);
        }
        return statuses[code];
    }
}