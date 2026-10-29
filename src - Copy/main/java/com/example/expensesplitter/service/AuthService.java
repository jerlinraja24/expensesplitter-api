package com.example.expensesplitter.service;

import com.example.expensesplitter.exception.ApiException;
import com.example.expensesplitter.model.Session;
import com.example.expensesplitter.model.User;
import com.example.expensesplitter.repository.SessionRepository;
import com.example.expensesplitter.repository.UserRepository;
import com.example.expensesplitter.util.Responses;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

@Service
public class AuthService {

    private static final String EMAIL_REGEX = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";

    private final UserRepository users;
    private final SessionRepository sessions;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository users, SessionRepository sessions) {
        this.users = users;
        this.sessions = sessions;
    }

    public Map<String, Object> register(String name, String email, String password) {
        String cleanName = name == null ? "" : name.trim();
        String cleanEmail = normalizeEmail(email);
        if (cleanName.isEmpty() || cleanName.length() > 60) {
            throw new ApiException(400, "Enter your name (up to 60 characters).");
        }
        if (password == null || password.length() < 8) {
            throw new ApiException(400, "Password must be at least 8 characters.");
        }
        if (users.findByEmail(cleanEmail).isPresent()) {
            throw new ApiException(400, "An account with this email already exists.");
        }
        User user = new User();
        user.setName(cleanName);
        user.setEmail(cleanEmail);
        user.setPasswordHash(encoder.encode(password));
        user.setCreatedAt(LocalDateTime.now());
        users.save(user);
        return startSession(user);
    }

    public Map<String, Object> login(String email, String password) {
        String cleanEmail = normalizeEmail(email);
        User user = users.findByEmail(cleanEmail)
                .orElseThrow(() -> new ApiException(401, "Incorrect email or password."));
        if (password == null || !encoder.matches(password, user.getPasswordHash())) {
            throw new ApiException(401, "Incorrect email or password.");
        }
        return startSession(user);
    }

    public void logout(String authHeader) {
        String token = tokenFrom(authHeader);
        if (token != null && sessions.existsById(token)) {
            sessions.deleteById(token);
        }
    }

    /** Returns the signed-in user's id, or throws 401 with a clear message. */
    public Long requireUserId(String authHeader) {
        String token = tokenFrom(authHeader);
        if (token == null) {
            throw new ApiException(401, "Please sign in first.");
        }
        Session session = sessions.findById(token)
                .orElseThrow(() -> new ApiException(401, "Please sign in first."));
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            sessions.deleteById(token);
            throw new ApiException(401, "Your session has expired. Please sign in again.");
        }
        return session.getUserId();
    }

    private Map<String, Object> startSession(User user) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getEncoder().encodeToString(bytes);
        sessions.save(new Session(token, user.getId(), LocalDateTime.now().plusHours(12)));
        return Responses.ok(
                "token", token,
                "user", Responses.map("email", user.getEmail(), "name", user.getName()));
    }

    private String normalizeEmail(String email) {
        String clean = email == null ? "" : email.trim().toLowerCase();
        if (clean.isEmpty() || clean.length() > 254 || !clean.matches(EMAIL_REGEX)) {
            throw new ApiException(400, "Enter a valid email address.");
        }
        return clean;
    }

    private String tokenFrom(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7).trim();
        return token.isEmpty() ? null : token;
    }
}
