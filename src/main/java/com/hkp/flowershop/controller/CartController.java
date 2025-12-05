package com.hkp.flowershop.controller;

import com.hkp.flowershop.dto.CartDto;
import com.hkp.flowershop.dto.CartItemsDto;
import com.hkp.flowershop.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/carts")
public class CartController {

    @Autowired
    private CartService cartService;

    // Get cart
    @GetMapping
    public CartDto getCart(Principal principal) {
        return cartService.getCartDto(principal.getName());
    }

    // Add item
    @PostMapping("/add")
    public CartDto addToCart(Principal principal, @RequestBody CartItemsDto cartItemDto) {
        return cartService.addToCart(principal.getName(), cartItemDto);
    }

    // Update quantity
    @PutMapping("/update")
    public CartDto updateCartItem(Principal principal,
                                  @RequestParam Long productId,
                                  @RequestParam int quantity) {
        return cartService.updateCartItem(principal.getName(), productId, quantity);
    }


    // Remove item
    @DeleteMapping("/remove/{productId}")
    public CartDto removeFromCart(Principal principal, @PathVariable Long productId) {
        return cartService.removeFromCart(principal.getName(), productId);
    }

    // Clear cart
    @DeleteMapping("/clear")
    public CartDto clearCart(Principal principal) {
        return cartService.clearCart(principal.getName());
    }

}
