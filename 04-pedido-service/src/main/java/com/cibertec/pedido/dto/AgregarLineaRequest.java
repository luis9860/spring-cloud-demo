package com.cibertec.pedido.dto;

public record AgregarLineaRequest(Long productoId, int cantidad, String notas) {}
