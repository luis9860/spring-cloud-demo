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
        Usuario u = new Usuario();
        u.setRestauranteId(restauranteId);
        u.setUsername(request.username());
        u.setPasswordHash(passwordEncoder.encode(request.password()));
        u.setRol(request.rol());
        u.setActivo(true);
        usuarioRepository.save(u);
        return Map.of("id", u.getId(), "username", u.getUsername(), "rol", u.getRol());
    }
}
