package com.manga.collectionBend.exceptions;

public class RefreshTokenExpiredException extends RuntimeException{
    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}
