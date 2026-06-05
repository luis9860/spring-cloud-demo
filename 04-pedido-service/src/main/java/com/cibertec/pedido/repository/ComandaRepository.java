package com.cibertec.pedido.repository;

import com.cibertec.pedido.entity.Comanda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComandaRepository extends JpaRepository<Comanda, Long> {
    List<Comanda> findBySesionMesaIdOrderByNumeroTurnoAsc(Long sesionMesaId);
    Optional<Comanda> findBySesionMesaIdAndEstado(Long sesionMesaId, String estado);
}
