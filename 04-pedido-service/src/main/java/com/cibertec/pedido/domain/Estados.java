package com.cibertec.pedido.domain;

public final class Estados {
    private Estados() {}

    public static final String MESA_LIBRE = "LIBRE";
    public static final String MESA_OCUPADA = "OCUPADA";

    public static final String COMANDA_BORRADOR = "BORRADOR";
    public static final String COMANDA_ENVIADA = "ENVIADA";
    public static final String COMANDA_EN_PREPARACION = "EN_PREPARACION";
    public static final String COMANDA_PARCIAL = "PARCIALMENTE_LISTA";
    public static final String COMANDA_LISTA = "LISTA";
    /** Mozo llevó los platos a la mesa (servido al cliente). */
    public static final String COMANDA_ENTREGADA = "ENTREGADA";

    public static final String LINEA_BORRADOR = "BORRADOR";
    public static final String LINEA_ENVIADA = "ENVIADA";
    public static final String LINEA_EN_PREPARACION = "EN_PREPARACION";
    public static final String LINEA_LISTA = "LISTA";
    public static final String LINEA_ENTREGADA = "ENTREGADA";
    public static final String LINEA_ANULADA = "ANULADA";
}
