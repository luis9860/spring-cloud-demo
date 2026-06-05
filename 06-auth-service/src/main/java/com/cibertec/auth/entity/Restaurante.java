package com.cibertec.auth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "restaurante")
public class Restaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String plan = "FREE";

    @Column(name = "limite_mesas", nullable = false)
    private int limiteMesas = 15;

    @Column(name = "modo_servicio_default", nullable = false, length = 1)
    private String modoServicioDefault = "A";

    @Column(nullable = false)
    private boolean activo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
    public int getLimiteMesas() { return limiteMesas; }
    public void setLimiteMesas(int limiteMesas) { this.limiteMesas = limiteMesas; }
    public String getModoServicioDefault() { return modoServicioDefault; }
    public void setModoServicioDefault(String modoServicioDefault) { this.modoServicioDefault = modoServicioDefault; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
