package com.scaler.EcomUserService.controller.controllerAdvice;

import com.scaler.EcomUserService.dto.ErrorResponseDTO;
import com.scaler.EcomUserService.exception.InvalidTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(value = InvalidTokenException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidTokenException(Exception ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO();
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setMessageCode(403);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }
}
