package com.cibertec.producto.repository;

import com.cibertec.producto.entity.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<ProductoEntity, Long> {
    List<ProductoEntity> findByRestauranteIdAndActivoTrue(Long restauranteId);
    List<ProductoEntity> findByRestauranteIdAndActivoTrueAndEstacion(Long restauranteId, String estacion);
    List<ProductoEntity> findByRestauranteIdAndActivoTrueAndCategoriaId(Long restauranteId, Long categoriaId);
    Optional<ProductoEntity> findByIdAndRestauranteIdAndActivoTrue(Long id, Long restauranteId);
}
