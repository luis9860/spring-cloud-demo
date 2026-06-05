package com.cibertec.pedido.repository;

import com.cibertec.pedido.entity.LineaPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LineaPedidoRepository extends JpaRepository<LineaPedido, Long> {
    List<LineaPedido> findByComandaId(Long comandaId);
    List<LineaPedido> findByEstadoInAndEstacionProducto(List<String> estados, String estacion);
    List<LineaPedido> findByEstadoIn(List<String> estados);
}
