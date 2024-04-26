package com.scaler.EcomUserService.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaler.EcomUserService.config.KafkaProducerConfig;
import com.scaler.EcomUserService.dto.SendEmailDto;
import com.scaler.EcomUserService.dto.UserDto;
import com.scaler.EcomUserService.exception.InvalidCredentialException;
import com.scaler.EcomUserService.exception.InvalidTokenException;
import com.scaler.EcomUserService.exception.UserAlreadyExistsWithGivenEmailException;
import com.scaler.EcomUserService.exception.UserNotFoundException;
import com.scaler.EcomUserService.mapper.UserEntityDTOMapper;
import com.scaler.EcomUserService.model.Session;
import com.scaler.EcomUserService.model.SessionStatus;
import com.scaler.EcomUserService.model.User;
import com.scaler.EcomUserService.repository.SessionRepository;
import com.scaler.EcomUserService.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMapAdapter;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class AuthService {
    private UserRepository userRepository;
    private SessionRepository sessionRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private KafkaProducerConfig kafkaProducerConfig;
    private ObjectMapper objectMapper;
    private SecretKey key;
    private MacAlgorithm algo = Jwts.SIG.HS256;


    public AuthService(UserRepository userRepository, SessionRepository sessionRepository ,
                       BCryptPasswordEncoder bCryptPasswordEncoder ,
                       KafkaProducerConfig kafkaProducerConfig ,
                       ObjectMapper objectMapper ,
                       SecretKey key) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.kafkaProducerConfig = kafkaProducerConfig;
        this.objectMapper = objectMapper;
        this.key = key;
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
        //For RandomString Token ,apache's commons-lang3 dependency is added in pom.xml for the below method
        //String token = RandomStringUtils.randomAlphanumeric(30);

        //Building a JWT Token
        /*
        MacAlgorithm algo = Jwts.SIG.HS256; // HS256 algo added for JWT
        SecretKey key = algo.key().build(); // generating the secret key
        */


        //start adding the claims
        Map<String, Object> jsonForJWT = new HashMap<>();


        LocalDateTime localDateTime = LocalDateTime.now();
        Date createdAt =  Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        Date expiryAt = Date.from(localDateTime.plusDays(3).atZone(ZoneId.systemDefault()).toInstant());

        jsonForJWT.put("userId", user.getId());
        jsonForJWT.put("roles", user.getRoles());
        jsonForJWT.put("createdAt", createdAt);
        jsonForJWT.put("expiryAt", expiryAt);

        String token = Jwts.builder()
                .claims(jsonForJWT) // added the claims
                .signWith(key,algo) // added the key and algo
                .compact(); //building the token


        //Session creation
        Session session = new Session();
        session.setSessionStatus(SessionStatus.ACTIVE);
        session.setToken(token);
        session.setUser(user);
        session.setLoginAt(createdAt);
        session.setExpiringAt(expiryAt);
        sessionRepository.save(session);
        // generating the response
        UserDto userDto = UserEntityDTOMapper.getUserDTOFromUserEntity(user);
        // setting up the headers
        MultiValueMapAdapter<String, String> headers = new MultiValueMapAdapter<>(new HashMap<>());
        headers.add(HttpHeaders.SET_COOKIE, "auth-token:" + token);


        ResponseEntity<UserDto> response = new ResponseEntity<>(userDto, headers, HttpStatus.OK);

        return response;
    }

    public ResponseEntity<String> logout(String token, Long userId) {
        // validations -> token exists, token is not expired, user exists else throw an exception
        Optional<User> userOptional = userRepository.findById(userId);
        if(userOptional.isEmpty()){
            throw new UserNotFoundException("User Not found with id : "+ userId);
        }
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, userId);
        if (sessionOptional.isEmpty() || sessionOptional.get().getSessionStatus().equals(SessionStatus.ENDED)) {
            throw new InvalidTokenException("token is invalid");
        }
        Session session = sessionOptional.get();
        session.setSessionStatus(SessionStatus.ENDED);
        sessionRepository.save(session);
        return ResponseEntity.ok("Logout Successful");
    }

    public UserDto signUp(String email, String password) throws JsonProcessingException {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if(userOptional.isPresent()){
            throw new UserAlreadyExistsWithGivenEmailException("User already exists with given email");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));

        User savedUser = userRepository.save(user);

        //If a user has signed up successfully , then push an event inside the queue with a particular topic
        SendEmailDto sendEmailDto = new SendEmailDto();
        sendEmailDto.setFrom("dev77.mailsender@gmail.com");
        sendEmailDto.setTo(savedUser.getEmail());
        sendEmailDto.setSubject("Signup Successful");
        sendEmailDto.setBody("Welcome to our platform");

        //kafkaProducerConfig.sendMessage("signUp" , objectMapper.writeValueAsString(sendEmailDto));
        //System.out.println("Sign-Up event happened , Sending msg to Kafka Topic:");

        return UserDto.from(savedUser);
    }


    public SessionStatus validate(String token, Long userId) {
        System.out.println("Initiating Token Validation..");
        //verifying from DB if session exists
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, userId);
        if (sessionOptional.isEmpty() || sessionOptional.get().getSessionStatus().equals(SessionStatus.ENDED)) {
            throw new InvalidTokenException("token is invalid");
        }

        //TODO - check expiry from claims [Completed]
        //Way 1 : Using Base64 Decoder
/*        String[] parts = token.split("\\.");
        // Decode the payload section.
        String payload = new String(Base64.getDecoder().decode(parts[1]));
        // Parse the JSON object.
        JSONObject jsonObject = new JSONObject(payload);
        long expiryTimeMillis = jsonObject.getLong("expiryAt");
        if(System.currentTimeMillis() > expiryTimeMillis){
            sessionOptional.get().setSessionStatus(SessionStatus.ENDED);
            throw new InvalidTokenException("Token has expired");
        }
*/
        //Way 2 : Jwts Parser -> parse the encoded JWT token to read the claims


        Jws<Claims> jws = Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
        Claims claims = jws.getPayload();
        Long expiryTimeMillis = claims.get("expiryAt" , Long.class);
        Long currentTimeMillis = System.currentTimeMillis();
        if(expiryTimeMillis < currentTimeMillis){
            Session expiredSession = sessionOptional.get();
            expiredSession.setSessionStatus(SessionStatus.ENDED);
            sessionRepository.save(expiredSession);
            throw new InvalidTokenException("token has expired");
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