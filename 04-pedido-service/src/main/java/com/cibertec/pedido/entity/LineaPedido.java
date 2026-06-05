package com.cibertec.pedido.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "linea_pedido")
public class LineaPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comanda_id", nullable = false)
    private Long comandaId;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "numero_silla", nullable = false)
    private int numeroSilla;

    @Column(nullable = false)
    private int cantidad = 1;

    @Column(length = 255)
    private String notas;

    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, length = 25)
    private String estado = "BORRADOR";

    @Column(name = "producto_nombre_snapshot", length = 120)
    private String productoNombreSnapshot;

    @Column(name = "estacion_producto", length = 20)
    private String estacionProducto;

    @Column(nullable = false)
    private boolean pagado = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getComandaId() { return comandaId; }
    public void setComandaId(Long comandaId) { this.comandaId = comandaId; }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public int getNumeroSilla() { return numeroSilla; }
    public void setNumeroSilla(int numeroSilla) { this.numeroSilla = numeroSilla; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getProductoNombreSnapshot() { return productoNombreSnapshot; }
    public void setProductoNombreSnapshot(String productoNombreSnapshot) { this.productoNombreSnapshot = productoNombreSnapshot; }
    public String getEstacionProducto() { return estacionProducto; }
    public void setEstacionProducto(String estacionProducto) { this.estacionProducto = estacionProducto; }
    public boolean isPagado() { return pagado; }
    public void setPagado(boolean pagado) { this.pagado = pagado; }
}
