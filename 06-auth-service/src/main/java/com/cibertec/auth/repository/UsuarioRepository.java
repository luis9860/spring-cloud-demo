package com.cibertec.auth.repository;

import com.cibertec.auth.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsernameAndActivoTrue(String username);
    List<Usuario> findByRestauranteIdOrderByUsernameAsc(Long restauranteId);
    Optional<Usuario> findByIdAndRestauranteId(Long id, Long restauranteId);
}
