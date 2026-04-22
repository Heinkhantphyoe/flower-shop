package com.hkp.flowershop.controller;

import com.hkp.flowershop.dto.CartDto;
import com.hkp.flowershop.dto.CartItemsDto;
import com.hkp.flowershop.dto.WishlistDto;
import com.hkp.flowershop.dto.response.ApiResponse;
import com.hkp.flowershop.dto.requests.MoveToCartRequest;
import com.hkp.flowershop.exceptions.BadRequestException;
import com.hkp.flowershop.exceptions.ResourceNotFoundException;
import com.hkp.flowershop.model.UserPrinciple;
import com.hkp.flowershop.service.CartService;
import com.hkp.flowershop.service.WishlistService;
import com.hkp.flowershop.service.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wishlists")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private CartService cartService;

    /**
     * GET /wishlists - Get all wishlist items for authenticated user
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistDto>>> getWishlist(Authentication authentication) {
        UserPrinciple userPrinciple = getAuthenticatedUser(authentication);
        List<WishlistDto> wishlist = wishlistService.getWishlist(userPrinciple.getId());
        return ResponseUtil.success(wishlist);
    }

    /**
     * POST /wishlists/{productId} - Add product to wishlist
     */
    @PostMapping("/{productId}")
    public ResponseEntity<?> addToWishlist(
            Authentication authentication,
            @PathVariable Long productId) {
        UserPrinciple userPrinciple = getAuthenticatedUser(authentication);
        try {
            WishlistDto wishlist = wishlistService.addToWishlist(userPrinciple.getId(), productId);
            return ResponseUtil.created(wishlist, "Product added to wishlist");
        } catch (BadRequestException e) {
            return ResponseUtil.badRequest(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    /**
     * DELETE /wishlists/{productId} - Remove product from wishlist
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeFromWishlist(
            Authentication authentication,
            @PathVariable Long productId) {
        UserPrinciple userPrinciple = getAuthenticatedUser(authentication);
        try {
            wishlistService.removeFromWishlist(userPrinciple.getId(), productId);
            return ResponseUtil.success("Product removed from wishlist");
        } catch (ResourceNotFoundException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    /**
     * POST /wishlists/{productId}/toggle - Toggle product in wishlist
     */
    @PostMapping("/{productId}/toggle")
    public ResponseEntity<?> toggleWishlist(
            Authentication authentication,
            @PathVariable Long productId) {
        UserPrinciple userPrinciple = getAuthenticatedUser(authentication);
        try {
            WishlistDto result = wishlistService.toggleWishlist(userPrinciple.getId(), productId);
            if (result == null) {
                return ResponseUtil.success("Product removed from wishlist");
            }
            return ResponseUtil.created(result, "Product added to wishlist");
        } catch (ResourceNotFoundException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    /**
     * GET /wishlists/{productId}/is-wishlisted - Check if product is in wishlist
     */
    @GetMapping("/{productId}/is-wishlisted")
    public ResponseEntity<ApiResponse<Map<String, Object>>> isInWishlist(
            Authentication authentication,
            @PathVariable Long productId) {
        UserPrinciple userPrinciple = getAuthenticatedUser(authentication);
        boolean isInWishlist = wishlistService.isInWishlist(userPrinciple.getId(), productId);
        return ResponseUtil.success(Map.of(
                "productId", productId,
                "isInWishlist", isInWishlist
        ));
    }

    /**
     * POST /wishlists/move-to-cart/{wishlistId} - Move wishlist item to cart
     */
    @PostMapping("/move-to-cart/{wishlistId}")
    public ResponseEntity<?> moveToCart(
            Authentication authentication,
            @PathVariable Long wishlistId,
            @RequestBody MoveToCartRequest request) {
        UserPrinciple userPrinciple = getAuthenticatedUser(authentication);

        if (request.getQuantity() <= 0) {
            return ResponseUtil.badRequest("Quantity must be greater than 0");
        }

        try {
            WishlistDto wishlistItem = wishlistService.getWishlistItem(wishlistId);

            CartItemsDto cartItem = new CartItemsDto(
                    wishlistItem.getProductName(),
                    request.getQuantity(),
                    wishlistItem.getPrice(),
                    wishlistItem.getImageUrl(),
                    wishlistItem.getProductId()
            );

            CartDto updatedCart = cartService.addToCart(userPrinciple.getEmail(), cartItem);

            wishlistService.removeFromWishlist(userPrinciple.getId(), wishlistItem.getProductId());

            return ResponseUtil.success(updatedCart, "Product moved to cart successfully");
        } catch (ResourceNotFoundException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    /**
     * DELETE /wishlists/clear - Clear entire wishlist
     */
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearWishlist(Authentication authentication) {
        UserPrinciple userPrinciple = getAuthenticatedUser(authentication);
        wishlistService.clearWishlist(userPrinciple.getId());
        return ResponseUtil.success("Wishlist cleared successfully");
    }

    private UserPrinciple getAuthenticatedUser(Authentication authentication) {
        return (UserPrinciple) authentication.getPrincipal();
    }
}
