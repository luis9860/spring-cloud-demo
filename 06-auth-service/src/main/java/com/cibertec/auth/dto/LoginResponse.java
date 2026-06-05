package com.cibertec.auth.dto;

public record LoginResponse(
        String token,
        String rol,
        Long restauranteId,
        String nombreRestaurante,
        String modoServicioDefault
) {}
