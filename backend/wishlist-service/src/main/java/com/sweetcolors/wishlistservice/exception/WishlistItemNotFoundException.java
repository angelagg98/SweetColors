package com.sweetcolors.wishlistservice.exception;

public class WishlistItemNotFoundException extends RuntimeException {

    public WishlistItemNotFoundException(Long id) {
        super("El item de wishlist con id " + id + " no existe");
    }
}