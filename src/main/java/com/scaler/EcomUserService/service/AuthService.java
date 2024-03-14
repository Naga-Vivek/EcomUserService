package com.scaler.EcomUserService.service;

import com.scaler.EcomUserService.dto.UserDto;
import com.scaler.EcomUserService.exception.InvalidCredentialException;
import com.scaler.EcomUserService.exception.UserNotFoundException;
import com.scaler.EcomUserService.mapper.UserEntityDTOMapper;
import com.scaler.EcomUserService.model.Session;
import com.scaler.EcomUserService.model.SessionStatus;
import com.scaler.EcomUserService.repository.SessionRepository;
import com.scaler.EcomUserService.repository.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMapAdapter;
import com.scaler.EcomUserService.model.User;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {
    private UserRepository userRepository;
    private SessionRepository sessionRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public AuthService(UserRepository userRepository, SessionRepository sessionRepository ,  BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public ResponseEntity<UserDto> login(String email, String password) {
        //Get User details from DB
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User for the given email doesn't exist");
        }

        User user = userOptional.get();
        // Verify the user password given at the time of login
        if (!bCryptPasswordEncoder.matches(password,user.getPassword())) {
            throw new InvalidCredentialException("Invalid Credentials");
        }
        //Invalidate previous session's token if active
        Optional<List<Session>> sessionsOptional = sessionRepository.findByUser_idAndSessionStatus(user.getId(),SessionStatus.ACTIVE);
        if(sessionsOptional.isPresent()){
            List<Session> activeSessions = sessionsOptional.get();
            for(Session session : activeSessions){
                session.setToken(null);
                session.setSessionStatus(SessionStatus.ENDED);
            }
        }
        //Token generation
        //apache's commons-lang3 dependency is added in pom.xml for the below method
        String token = RandomStringUtils.randomAlphanumeric(30);
        //Session creation
        Session session = new Session();
        session.setSessionStatus(SessionStatus.ACTIVE);
        session.setToken(token);
        session.setUser(user);
        session.setLoginAt(new Date());
        sessionRepository.save(session);
        // generating the response
        UserDto userDto = UserEntityDTOMapper.getUserDTOFromUserEntity(user);
        // setting up the headers
        MultiValueMapAdapter<String, String> headers = new MultiValueMapAdapter<>(new HashMap<>());
        headers.add(HttpHeaders.SET_COOKIE, "auth-token:" + token);


        ResponseEntity<UserDto> response = new ResponseEntity<>(userDto, headers, HttpStatus.OK);

        return response;
    }

    public ResponseEntity<Void> logout(String token, Long userId) {
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, userId);

        if (sessionOptional.isEmpty()) {
            return null;
        }

        Session session = sessionOptional.get();

        session.setSessionStatus(SessionStatus.ENDED);

        sessionRepository.save(session);

        return ResponseEntity.ok().build();
    }

    public UserDto signUp(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));

        User savedUser = userRepository.save(user);

        return UserDto.from(savedUser);
    }

    public SessionStatus validate(String token, Long userId) {
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, userId);

        if (sessionOptional.isEmpty()) {
            return null;
        }

        return SessionStatus.ACTIVE;
    }

    public ResponseEntity<List<Session>> getAllSessions() {
        List<Session> sessions = sessionRepository.findAll();
        return ResponseEntity.ok(sessions);
    }

    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
    /*
    MultiValueMapAdapter is map with single key and multiple values
    Headers
    Key     Value
    Token   """
    Accept  application/json, text, images
 */
}