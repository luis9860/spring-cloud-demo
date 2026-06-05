package com.cibertec.producto.repository;

import com.cibertec.producto.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByRestauranteId(Long restauranteId);
}
