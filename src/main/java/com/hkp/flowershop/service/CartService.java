package com.hkp.flowershop.service;

import com.hkp.flowershop.dto.CartDto;
import com.hkp.flowershop.dto.CartItemsDto;
import com.hkp.flowershop.model.Cart;
import com.hkp.flowershop.model.CartItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private String getCartKey(String email) {
        return "cart:" + email;
    }

    // Get cart from Redis or create new
    private Cart getCart(String email) {
        Object obj = redisTemplate.opsForValue().get(getCartKey(email));
        if (obj != null) {
            return (Cart) obj;
        }
        Cart newCart = new Cart(email, new ArrayList<>());
        redisTemplate.opsForValue().set(getCartKey(email), newCart);
        return newCart;
    }

    // Convert Cart to CartDto
    private CartDto mapToDto(Cart cart) {
        return new CartDto(
                cart.getCartItems().stream()
                        .map(ci -> new CartItemsDto(
                                ci.getName(),
                                ci.getQuantity(),
                                ci.getPrice(),
                                ci.getImageUrl(),
                                ci.getProductId()
                                ))
                        .collect(Collectors.toList())
        );
    }

    // Get CartDto
    public CartDto getCartDto(String email) {
        Cart cart = getCart(email);
        return mapToDto(cart);
    }

    // Add item to cart
    public CartDto addToCart(String email, CartItemsDto cartItemDto) {
        Cart cart = getCart(email);

        boolean found = false;
        for (CartItem item : cart.getCartItems()) {
            if (item.getProductId().equals(cartItemDto.getProductId())) {
                item.setQuantity(item.getQuantity() + cartItemDto.getQuantity());
                found = true;
                break;
            }
        }

        if (!found) {
            CartItem newItem = new CartItem(
                    cartItemDto.getProductId(),
                    cartItemDto.getProductName(),
                    cartItemDto.getQuantity(),
                    cartItemDto.getPrice(),
                    cartItemDto.getImageUrl()
            );
            cart.getCartItems().add(newItem);
        }

        redisTemplate.opsForValue().set(getCartKey(email), cart);
        return mapToDto(cart);
    }

    // Update cart item quantity
    public CartDto updateCartItem(String email, Long productId, int quantity) {
        Cart cart = getCart(email);

        if(quantity <= 0){
            cart.getCartItems().removeIf(item -> item.getProductId().equals(productId));
        }else{
            for (CartItem item : cart.getCartItems()) {
                if (item.getProductId().equals(productId)) {
                    // Update quantity
                    item.setQuantity(quantity);
                    break;
                }
            }
        }

        redisTemplate.opsForValue().set(getCartKey(email), cart);
        return mapToDto(cart);
    }


    // Remove item from cart
    public CartDto removeFromCart(String email, Long productId) {
        Cart cart = getCart(email);
        cart.getCartItems().removeIf(item -> item.getProductId().equals(productId));
        redisTemplate.opsForValue().set(getCartKey(email), cart);
        return mapToDto(cart);
    }

    // Clear cart
    public CartDto clearCart(String email) {
        redisTemplate.delete(getCartKey(email));
        return new CartDto( new ArrayList<>());
    }

}
