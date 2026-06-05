package com.cibertec.pedido.repository;

import com.cibertec.pedido.entity.Silla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SillaRepository extends JpaRepository<Silla, Long> {
    @Query("SELECT COALESCE(MAX(s.numero), 0) FROM Silla s WHERE s.mesaId = :mesaId AND s.activa = true")
    int maxNumero(@Param("mesaId") Long mesaId);
    List<Silla> findByMesaIdAndActivaTrueOrderByNumeroAsc(Long mesaId);
    Optional<Silla> findByMesaIdAndNumeroAndActivaTrue(Long mesaId, int numero);
    Optional<Silla> findByCodigoAndActivaTrue(String codigo);
    long countByMesaIdAndActivaTrue(Long mesaId);
    boolean existsByMesaIdAndNumero(Long mesaId, int numero);
}
