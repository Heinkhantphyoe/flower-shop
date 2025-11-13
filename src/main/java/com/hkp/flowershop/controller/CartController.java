package com.hkp.flowershop.controller;

import com.hkp.flowershop.dto.CartDto;
import com.hkp.flowershop.dto.CartItemsDto;
import com.hkp.flowershop.dto.OrderDto;
import com.hkp.flowershop.dto.requests.CreateOrderRequest;
import com.hkp.flowershop.exceptions.BadRequestException;
import com.hkp.flowershop.model.Cart;
import com.hkp.flowershop.model.Order;
import com.hkp.flowershop.service.CartService;
import com.hkp.flowershop.service.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/carts")
public class CartController {

    @Autowired
    CartService cartService;

    @GetMapping()
    public ResponseEntity<CartDto> getCart(Principal principal) {
        Cart cart = cartService.getCartByEmail(principal.getName());

        List<CartItemsDto> items = cart.getCartItems().stream()
                .map(item -> new CartItemsDto(
                        item.getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getProduct().getPrice(),
                        item.getProduct().getImageUrl(),
                        item.getProduct().getId()
                ))
                .toList();

        CartDto response = new CartDto(
                cart.getId(),
                cart.getUser().getId(),
                items
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<CartDto> addToCart(Principal principal,
                                          @RequestParam Long productId,
                                          @RequestParam int quantity) {
        Cart cart = cartService.addToCart(principal.getName(), productId, quantity);
        List<CartItemsDto> items = cart.getCartItems().stream()
                .map(item -> new CartItemsDto(
                        item.getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getProduct().getPrice(),
                        item.getProduct().getImageUrl(),
                        item.getProduct().getId()
                ))
                .toList();

        CartDto response = new CartDto(
                cart.getId(),
                cart.getUser().getId(),
                items
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update")
    public ResponseEntity<CartDto> updateCartItem(Principal principal,
                                               @RequestParam Long cartItemId,
                                               @RequestParam int quantity) {
        Cart cart = cartService.updateCartItem(principal.getName(), cartItemId, quantity);
        List<CartItemsDto> items = cart.getCartItems().stream()
                .map(item -> new CartItemsDto(
                        item.getId(),
                        item.getProduct().getName(),  // assuming relation with Product
                        item.getQuantity(),
                        item.getProduct().getPrice(),
                        item.getProduct().getImageUrl(),
                        item.getProduct().getId()
                ))
                .toList();

        CartDto response = new CartDto(
                cart.getId(),
                cart.getUser().getId(),
                items
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> removeCartItem(Principal principal,
                                                 @RequestParam Long cartItemId) {
        cartService.removeCartItem(principal.getName(), cartItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(Principal principal) {
        cartService.clearCart(principal.getName());
        return ResponseEntity.ok("Cart cleared");
    }

}
