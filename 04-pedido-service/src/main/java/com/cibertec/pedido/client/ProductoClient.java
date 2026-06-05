package com.cibertec.pedido.client;

import com.cibertec.pedido.dto.Producto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * Cliente Feign hacia producto-service (plan técnico Comandas de Restaurantes).
 */
@FeignClient(name = "producto-service")
public interface ProductoClient {

    @GetMapping("/productos")
    List<Producto> listar(@RequestHeader("X-Restaurante-Id") Long restauranteId);

    @GetMapping("/productos/{id}")
    Producto obtenerProducto(@PathVariable("id") Long id);
}
