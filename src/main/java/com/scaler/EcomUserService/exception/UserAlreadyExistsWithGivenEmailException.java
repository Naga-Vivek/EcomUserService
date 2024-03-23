package com.scaler.EcomUserService.exception;

public class UserAlreadyExistsWithGivenEmailException extends RuntimeException {
    public UserAlreadyExistsWithGivenEmailException(String message) {
        super(message);
    }
}
