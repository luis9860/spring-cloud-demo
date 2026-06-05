package com.cibertec.pedido.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "sesion_mesa")
public class SesionMesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mesa_id", nullable = false)
    private Long mesaId;

    @Column(name = "restaurante_id", nullable = false)
    private Long restauranteId;

    @Column(name = "abierta_en", nullable = false)
    private Instant abiertaEn = Instant.now();

    @Column(name = "cerrada_en")
    private Instant cerradaEn;

    @Column(nullable = false)
    private boolean activa = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMesaId() { return mesaId; }
    public void setMesaId(Long mesaId) { this.mesaId = mesaId; }
    public Long getRestauranteId() { return restauranteId; }
    public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }
    public Instant getAbiertaEn() { return abiertaEn; }
    public void setAbiertaEn(Instant abiertaEn) { this.abiertaEn = abiertaEn; }
    public Instant getCerradaEn() { return cerradaEn; }
    public void setCerradaEn(Instant cerradaEn) { this.cerradaEn = cerradaEn; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
