package com.cibertec.pedido.repository;

import com.cibertec.pedido.entity.ComensalSilla;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComensalSillaRepository extends JpaRepository<ComensalSilla, Long> {
    Optional<ComensalSilla> findBySesionMesaIdAndCodigoSilla(Long sesionMesaId, String codigoSilla);
    void deleteBySesionMesaId(Long sesionMesaId);
}
