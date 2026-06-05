package com.cibertec.producto.dto;

import java.math.BigDecimal;

public record CrearProductoRequest(
        String nombre,
        BigDecimal precio,
        Long categoriaId,
        String estacion
) {}
