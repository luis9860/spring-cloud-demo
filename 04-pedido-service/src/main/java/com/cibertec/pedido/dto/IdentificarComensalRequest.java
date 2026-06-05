package com.cibertec.pedido.dto;

public record IdentificarComensalRequest(
        String mesaCodigo,
        String codigoSilla,
        Boolean abrirMesaSiLibre
) {}
