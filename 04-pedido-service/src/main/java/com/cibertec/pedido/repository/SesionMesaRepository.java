package com.cibertec.pedido.repository;

import com.cibertec.pedido.entity.SesionMesa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SesionMesaRepository extends JpaRepository<SesionMesa, Long> {
    Optional<SesionMesa> findByMesaIdAndActivaTrue(Long mesaId);
}
