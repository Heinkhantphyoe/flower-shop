package com.hkp.flowershop.service;

import com.hkp.flowershop.dto.WishlistDto;
import com.hkp.flowershop.exceptions.BadRequestException;
import com.hkp.flowershop.exceptions.ResourceNotFoundException;
import com.hkp.flowershop.model.Product;
import com.hkp.flowershop.model.User;
import com.hkp.flowershop.model.Wishlist;
import com.hkp.flowershop.repository.ProductRepo;
import com.hkp.flowershop.repository.UserRepo;
import com.hkp.flowershop.repository.WishlistRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepo wishlistRepo;
    private final UserRepo userRepo;
    private final ProductRepo productRepo;

    /**
     * Get all wishlist items for a user
     */
    public List<WishlistDto> getWishlist(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return wishlistRepo.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Add a product to wishlist (with duplicate check)
     */
    @Transactional
    public WishlistDto addToWishlist(Long userId, Long productId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (wishlistRepo.existsByUserIdAndProductId(userId, productId)) {
            throw new BadRequestException("Product already in wishlist");
        }

        Wishlist wishlist = new Wishlist(user, product);
        Wishlist saved = wishlistRepo.save(wishlist);
        log.info("Added product {} to wishlist for user {}", productId, userId);

        return mapToDto(saved);
    }

    /**
     * Remove a product from wishlist
     */
    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        if (!wishlistRepo.existsByUserIdAndProductId(userId, productId)) {
            throw new ResourceNotFoundException("Wishlist item not found");
        }

        wishlistRepo.deleteByUserIdAndProductId(userId, productId);
        log.info("Removed product {} from wishlist for user {}", productId, userId);
    }

    /**
     * Toggle product in wishlist (add if not exists, remove if exists)
     */
    @Transactional
    public WishlistDto toggleWishlist(Long userId, Long productId) {
        if (wishlistRepo.existsByUserIdAndProductId(userId, productId)) {
            removeFromWishlist(userId, productId);
            return null;
        } else {
            return addToWishlist(userId, productId);
        }
    }

    /**
     * Check if product is in wishlist
     */
    public boolean isInWishlist(Long userId, Long productId) {
        return wishlistRepo.existsByUserIdAndProductId(userId, productId);
    }

    /**
     * Get single wishlist item by ID
     */
    public WishlistDto getWishlistItem(Long wishlistId) {
        Wishlist wishlist = wishlistRepo.findById(wishlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist item not found"));
        return mapToDto(wishlist);
    }

    /**
     * Clear entire wishlist for a user
     */
    @Transactional
    public void clearWishlist(Long userId) {
        List<Wishlist> items = wishlistRepo.findByUserId(userId);
        wishlistRepo.deleteAll(items);
        log.info("Cleared wishlist for user {}", userId);
    }

    // Helper method to map entity to DTO
    private WishlistDto mapToDto(Wishlist wishlist) {
        return new WishlistDto(
                wishlist.getId(),
                wishlist.getProduct().getId(),
                wishlist.getProduct().getName(),
                wishlist.getProduct().getDescription(),
                wishlist.getProduct().getPrice(),
                wishlist.getProduct().getImageUrl(),
                wishlist.getProduct().getStock(),
                wishlist.getProduct().getCategory().getName(),
                wishlist.getCreatedAt()
        );
    }
}
