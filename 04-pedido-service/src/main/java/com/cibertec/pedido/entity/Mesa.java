package com.cibertec.pedido.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "mesa", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"restaurante_id", "numero"}),
        @UniqueConstraint(columnNames = {"restaurante_id", "codigo"})
})
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurante_id", nullable = false)
    private Long restauranteId;

    @Column(nullable = false)
    private int numero;

    @Column(nullable = false, length = 20)
    private String codigo;

    @Column(name = "capacidad_sillas", nullable = false)
    private int capacidadSillas = 4;

    @Column(nullable = false, length = 20)
    private String estado = "LIBRE";

    @Column(name = "qr_token", nullable = false, unique = true, length = 64)
    private String qrToken;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRestauranteId() { return restauranteId; }
    public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public int getCapacidadSillas() { return capacidadSillas; }
    public void setCapacidadSillas(int capacidadSillas) { this.capacidadSillas = capacidadSillas; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getQrToken() { return qrToken; }
    public void setQrToken(String qrToken) { this.qrToken = qrToken; }
}
