package com.cibertec.auth.web;

import com.cibertec.auth.dto.CrearUsuarioRequest;
import com.cibertec.auth.dto.LoginRequest;
import com.cibertec.auth.dto.LoginResponse;
import com.cibertec.auth.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/usuarios")
    public Map<String, Object> crearUsuario(
            @RequestBody CrearUsuarioRequest request,
            @RequestHeader(value = "X-Rol", defaultValue = "") String rol,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return authService.crearUsuario(request, rol, restauranteId);
    }
}
