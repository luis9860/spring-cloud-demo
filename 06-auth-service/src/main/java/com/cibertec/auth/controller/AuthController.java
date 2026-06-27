package com.cibertec.auth.controller;

import com.cibertec.auth.dto.CrearUsuarioRequest;
import com.cibertec.auth.dto.LoginRequest;
import com.cibertec.auth.dto.LoginResponse;
import com.cibertec.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Login y administración de usuarios")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token generado"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/usuarios")
    @Operation(summary = "Crear usuario (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Usuario creado")
    public Map<String, Object> crearUsuario(
            @RequestBody CrearUsuarioRequest request,
            @RequestHeader(value = "X-Rol", defaultValue = "") String rol,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return authService.crearUsuario(request, rol, restauranteId);
    }

    @GetMapping("/usuarios")
    @Operation(summary = "Listar usuarios del restaurante (ADMIN)")
    public List<Map<String, Object>> listarUsuarios(
            @RequestHeader(value = "X-Rol", defaultValue = "") String rol,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return authService.listarUsuarios(rol, restauranteId);
    }

    @PatchMapping("/usuarios/{id}/desactivar")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar usuario (ADMIN)")
    @ApiResponse(responseCode = "204", description = "Usuario desactivado")
    public void desactivarUsuario(
            @PathVariable Long id,
            @RequestHeader(value = "X-Rol", defaultValue = "") String rol,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        authService.desactivarUsuario(id, rol, restauranteId);
    }
}
