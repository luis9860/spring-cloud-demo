package com.cibertec.pedido.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "comanda")
public class Comanda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sesion_mesa_id", nullable = false)
    private Long sesionMesaId;

    @Column(name = "numero_turno", nullable = false)
    private int numeroTurno = 1;

    @Column(nullable = false, length = 25)
    private String origen = "MOZO";

    @Column(nullable = false, length = 25)
    private String estado = "BORRADOR";

    @Column(name = "modo_servicio", nullable = false, length = 1)
    private String modoServicio = "A";

    @Column(name = "enviada_en")
    private Instant enviadaEn;

    @Column(name = "lista_para_servir_en")
    private Instant listaParaServirEn;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSesionMesaId() { return sesionMesaId; }
    public void setSesionMesaId(Long sesionMesaId) { this.sesionMesaId = sesionMesaId; }
    public int getNumeroTurno() { return numeroTurno; }
    public void setNumeroTurno(int numeroTurno) { this.numeroTurno = numeroTurno; }
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getModoServicio() { return modoServicio; }
    public void setModoServicio(String modoServicio) { this.modoServicio = modoServicio; }
    public Instant getEnviadaEn() { return enviadaEn; }
    public void setEnviadaEn(Instant enviadaEn) { this.enviadaEn = enviadaEn; }
    public Instant getListaParaServirEn() { return listaParaServirEn; }
    public void setListaParaServirEn(Instant listaParaServirEn) { this.listaParaServirEn = listaParaServirEn; }
}
