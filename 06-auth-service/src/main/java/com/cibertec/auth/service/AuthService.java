package com.cibertec.auth.service;

import com.cibertec.auth.dto.CrearUsuarioRequest;
import com.cibertec.auth.dto.LoginRequest;
import com.cibertec.auth.dto.LoginResponse;
import com.cibertec.auth.entity.Restaurante;
import com.cibertec.auth.entity.Usuario;
import com.cibertec.auth.repository.RestauranteRepository;
import com.cibertec.auth.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.List;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RestauranteRepository restauranteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       RestauranteRepository restauranteRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.restauranteRepository = restauranteRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsernameAndActivoTrue(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));
        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
        }
        Restaurante restaurante = restauranteRepository.findById(usuario.getRestauranteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurante no encontrado"));
        String token = jwtService.generarToken(
                usuario.getId(), usuario.getUsername(), usuario.getRol(), usuario.getRestauranteId());
        return new LoginResponse(
                token,
                usuario.getRol(),
                restaurante.getId(),
                restaurante.getNombre(),
                restaurante.getModoServicioDefault()
        );
    }

    public Map<String, Object> crearUsuario(CrearUsuarioRequest request, String rolSolicitante, Long restauranteId) {
        if (!"ADMIN".equals(rolSolicitante)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo ADMIN puede crear usuarios");
        }
        if (request.username() == null || request.username().isBlank()
                || request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario y password son obligatorios");
        }
        String rol = request.rol() != null ? request.rol().toUpperCase() : "";
        if (!List.of("ADMIN", "MOZO", "COCINERO", "BARRA").contains(rol)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no permitido");
        }
        Usuario u = new Usuario();
        u.setRestauranteId(restauranteId);
        u.setUsername(request.username().trim());
        u.setPasswordHash(passwordEncoder.encode(request.password()));
        u.setRol(rol);
        u.setActivo(true);
        usuarioRepository.save(u);
        return usuarioToMap(u);
    }

    public List<Map<String, Object>> listarUsuarios(String rolSolicitante, Long restauranteId) {
        if (!"ADMIN".equals(rolSolicitante)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo ADMIN puede listar usuarios");
        }
        return usuarioRepository.findByRestauranteIdOrderByUsernameAsc(restauranteId)
                .stream()
                .map(this::usuarioToMap)
                .toList();
    }

    public void desactivarUsuario(Long usuarioId, String rolSolicitante, Long restauranteId) {
        if (!"ADMIN".equals(rolSolicitante)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo ADMIN puede desactivar usuarios");
        }
        Usuario usuario = usuarioRepository.findByIdAndRestauranteId(usuarioId, restauranteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    private Map<String, Object> usuarioToMap(Usuario u) {
        return Map.of(
                "id", u.getId(),
                "username", u.getUsername(),
                "rol", u.getRol(),
                "activo", u.isActivo());
    }
}
