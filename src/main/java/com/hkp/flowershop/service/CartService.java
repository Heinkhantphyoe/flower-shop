package com.hkp.flowershop.service;

import com.hkp.flowershop.model.Cart;
import com.hkp.flowershop.model.CartItem;
import com.hkp.flowershop.model.Product;
import com.hkp.flowershop.model.User;
import com.hkp.flowershop.repository.CartItemsRepo;
import com.hkp.flowershop.repository.CartRepo;
import com.hkp.flowershop.repository.ProductRepo;
import com.hkp.flowershop.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepo cartRepository;

    private final CartItemsRepo cartItemRepository;

    private final ProductRepo productRepository;

    private final UserRepo userRepository;

    public Cart addToCart(String email, Long productId, int quantity) {
        Cart cart = getCartByEmail(email);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem existingItem = cart.getCartItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.getCartItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

    public Cart updateCartItem(String email, Long cartItemId, int quantity) {
        Cart cart = getCartByEmail(email);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Item does not belong to this user's cart");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
            return cart;
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        return cart;
    }

    public void removeCartItem(String email, Long cartItemId) {
        Cart cart = getCartByEmail(email);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Item does not belong to this user's cart");
        }

        cartItemRepository.delete(item);
    }

    public void clearCart(String email) {
        Cart cart = getCartByEmail(email);
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    public Cart getCartByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

    }
}
