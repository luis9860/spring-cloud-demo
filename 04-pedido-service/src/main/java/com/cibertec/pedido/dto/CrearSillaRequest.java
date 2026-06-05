package com.cibertec.pedido.dto;

/** Si numero es null, se asigna el siguiente (1, 2, 3…) en esa mesa. */
public record CrearSillaRequest(Integer numero) {}
