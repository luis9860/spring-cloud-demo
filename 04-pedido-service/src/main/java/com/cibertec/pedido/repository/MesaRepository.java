package com.cibertec.pedido.repository;

import com.cibertec.pedido.entity.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MesaRepository extends JpaRepository<Mesa, Long> {
    List<Mesa> findByRestauranteIdOrderByNumeroAsc(Long restauranteId);

    @Query("SELECT COALESCE(MAX(m.numero), 0) FROM Mesa m WHERE m.restauranteId = :restauranteId AND m.numero > 0")
    int maxNumero(@Param("restauranteId") Long restauranteId);
    long countByRestauranteId(Long restauranteId);
    Optional<Mesa> findByIdAndRestauranteId(Long id, Long restauranteId);
    Optional<Mesa> findByQrToken(String qrToken);
    Optional<Mesa> findByRestauranteIdAndCodigo(Long restauranteId, String codigo);
    boolean existsByRestauranteIdAndCodigo(Long restauranteId, String codigo);
    boolean existsByRestauranteIdAndNumero(Long restauranteId, int numero);
}
