package com.cibertec.producto.dto;

import java.math.BigDecimal;

public record ProductoDto(
        Long id,
        String nombre,
        BigDecimal precio,
        Long categoriaId,
        String estacion
) {}
