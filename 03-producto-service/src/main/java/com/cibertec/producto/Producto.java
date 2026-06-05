package com.cibertec.producto;

/** DTO simple de producto (Feign / pedidos/simular). */
public record Producto(Long id, String nombre, double precio, String estacion) {}
