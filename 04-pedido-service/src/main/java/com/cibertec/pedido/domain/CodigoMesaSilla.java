package com.cibertec.pedido.domain;

public final class CodigoMesaSilla {
    private CodigoMesaSilla() {}

    public static String codigoMesaPorNumero(int numero) {
        return "M" + String.format("%02d", numero);
    }

    public static String codigoMesaNormalizado(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Codigo de mesa requerido");
        }
        return codigo.trim().toUpperCase();
    }

    /** Codigo silla = codigo mesa + sufijo silla (ej. M01-S02). */
    public static String codigoSilla(String codigoMesa, int numeroSilla) {
        return codigoMesaNormalizado(codigoMesa) + "-S" + String.format("%02d", numeroSilla);
    }
}
