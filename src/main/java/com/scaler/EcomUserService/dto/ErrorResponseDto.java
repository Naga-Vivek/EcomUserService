package com.scaler.EcomUserService.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ErrorResponseDto {
    private String message;
    private int messageCode;

}
