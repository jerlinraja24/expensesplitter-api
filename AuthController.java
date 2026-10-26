package com.example.expensesplitter.controller;

import com.example.expensesplitter.service.AuthService;
import com.example.expensesplitter.util.Input;
import com.example.expensesplitter.util.Responses;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, Object> body) {
        return auth.register(Input.text(body, "name"), Input.text(body, "email"), Input.raw(body, "password"));
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> body) {
        return auth.login(Input.text(body, "email"), Input.raw(body, "password"));
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestHeader(value = "Authorization", required = false) String header) {
        auth.logout(header);
        return Responses.ok();
    }
}
