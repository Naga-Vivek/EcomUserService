package com.scaler.EcomUserService.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scaler.EcomUserService.dto.*;
import com.scaler.EcomUserService.model.Session;
import com.scaler.EcomUserService.model.SessionStatus;
import com.scaler.EcomUserService.model.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.scaler.EcomUserService.service.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<String> welcome() {
        return ResponseEntity.ok("Welcome to User Service");
    }
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody LoginRequestDto request) {
        return authService.login(request.getEmail(), request.getPassword());
    }

    @PostMapping("/logout/{id}")
    public ResponseEntity<String> logout(@PathVariable long id, @RequestHeader(value = HttpHeaders.AUTHORIZATION,required = true) String token) {
        return authService.logout(token, id);
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signUp(@RequestBody SignUpRequestDto request) throws JsonProcessingException {
        UserDto userDto = authService.signUp(request.getEmail(), request.getPassword());
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @PostMapping("/validate")
    public ResponseEntity<SessionStatus> validateToken(@RequestBody ValidateTokenRequestDto request) {
        SessionStatus sessionStatus = authService.validate(request.getToken(), request.getUserId());

        return new ResponseEntity<>(sessionStatus, HttpStatus.OK);
    }
    //below APIs are only for learning purposes, should not be present in actual systems
    @GetMapping("/session")
    public ResponseEntity<List<Session>> getAllSessions(){
        return authService.getAllSessions();
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(){
        return authService.getAllUsers();
    }
}