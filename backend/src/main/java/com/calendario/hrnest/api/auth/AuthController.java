package com.calendario.hrnest.api.auth;

import com.calendario.hrnest.application.auth.AuthResult;
import com.calendario.hrnest.application.auth.LoginCommand;
import com.calendario.hrnest.application.auth.LoginUseCase;
import com.calendario.hrnest.application.auth.RegisterCommand;
import com.calendario.hrnest.application.auth.RegisterUserUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase, LoginUseCase loginUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResult result = registerUserUseCase.execute(
                new RegisterCommand(request.email(), request.password(), request.firstName(), request.lastName()));
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(result.token()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResult result = loginUseCase.execute(new LoginCommand(request.email(), request.password()));
        return ResponseEntity.ok(new AuthResponse(result.token()));
    }
}
