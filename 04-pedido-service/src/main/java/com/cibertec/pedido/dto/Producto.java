package com.cibertec.pedido.dto;

/** Copia del DTO de producto para deserializar la respuesta del otro servicio. */
public record Producto(Long id, String nombre, double precio, String estacion) {}
