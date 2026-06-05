package com.cibertec.auth.config;

import com.cibertec.auth.entity.Restaurante;
import com.cibertec.auth.entity.Usuario;
import com.cibertec.auth.repository.RestauranteRepository;
import com.cibertec.auth.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner init(RestauranteRepository restauranteRepository,
                           UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder) {
        return args -> {
            if (restauranteRepository.count() > 0) {
                return;
            }
            Restaurante r = new Restaurante();
            r.setNombre("Restaurante Piloto Lima");
            r.setPlan("FREE");
            r.setLimiteMesas(15);
            r.setModoServicioDefault("A");
            r.setActivo(true);
            restauranteRepository.save(r);

            crearUsuario(usuarioRepository, passwordEncoder, r.getId(), "admin", "admin123", "ADMIN");
            crearUsuario(usuarioRepository, passwordEncoder, r.getId(), "mozo1", "mozo123", "MOZO");
            crearUsuario(usuarioRepository, passwordEncoder, r.getId(), "cocina1", "cocina123", "COCINERO");
        };
    }

    private void crearUsuario(UsuarioRepository repo, PasswordEncoder encoder,
                              Long restauranteId, String user, String pass, String rol) {
        Usuario u = new Usuario();
        u.setRestauranteId(restauranteId);
        u.setUsername(user);
        u.setPasswordHash(encoder.encode(pass));
        u.setRol(rol);
        u.setActivo(true);
        repo.save(u);
    }
}
