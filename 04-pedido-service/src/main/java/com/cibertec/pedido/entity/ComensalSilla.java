package com.cibertec.pedido.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "comensal_silla", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"sesion_mesa_id", "codigo_silla"})
})
public class ComensalSilla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sesion_mesa_id", nullable = false)
    private Long sesionMesaId;

    @Column(name = "codigo_silla", nullable = false, length = 32)
    private String codigoSilla;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String apellido;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSesionMesaId() { return sesionMesaId; }
    public void setSesionMesaId(Long sesionMesaId) { this.sesionMesaId = sesionMesaId; }
    public String getCodigoSilla() { return codigoSilla; }
    public void setCodigoSilla(String codigoSilla) { this.codigoSilla = codigoSilla; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
}
