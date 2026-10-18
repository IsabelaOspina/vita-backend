package org.example.vitabackend.service;

import org.example.vitabackend.dto.request.LoginRequest;
import org.example.vitabackend.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
}