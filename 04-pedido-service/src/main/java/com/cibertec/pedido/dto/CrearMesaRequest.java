package com.cibertec.pedido.dto;

/** El número de mesa (1, 2, 3…) se asigna automáticamente al crear. */
public record CrearMesaRequest(int capacidadSillas, String codigo) {}
