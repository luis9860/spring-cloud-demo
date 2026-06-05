package com.cibertec.pedido.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "silla", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"mesa_id", "numero"}),
        @UniqueConstraint(columnNames = {"codigo"})
})
public class Silla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mesa_id", nullable = false)
    private Long mesaId;

    @Column(nullable = false)
    private int numero;

    @Column(nullable = false, length = 32)
    private String codigo;

    @Column(nullable = false)
    private boolean activa = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMesaId() { return mesaId; }
    public void setMesaId(Long mesaId) { this.mesaId = mesaId; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
