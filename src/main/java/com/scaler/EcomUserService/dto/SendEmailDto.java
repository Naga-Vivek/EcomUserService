package com.scaler.EcomUserService.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class SendEmailDto {
    private String from;
    private String to;
    private String subject;
    private String body;
}
