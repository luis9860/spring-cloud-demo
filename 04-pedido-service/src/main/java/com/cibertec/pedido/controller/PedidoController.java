package com.cibertec.pedido.controller;

import com.cibertec.pedido.client.ProductoClient;
import com.cibertec.pedido.dto.Producto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/pedidos")
@Tag(name = "Pedidos", description = "Simulación Feign entre microservicios")
public class PedidoController {

    private final ProductoClient productoClient;

    @Value("${pedido.igv:18}")
    private int igv;

    public PedidoController(ProductoClient productoClient) {
        this.productoClient = productoClient;
    }

    @GetMapping("/simular/{productoId}")
    @Operation(summary = "Simular pedido consultando producto-service vía Feign")
    @ApiResponse(responseCode = "200", description = "Cálculo con IGV aplicado")
    public Map<String, Object> simular(@PathVariable Long productoId,
                                       @RequestParam(defaultValue = "1") int cantidad) {
        Producto producto = productoClient.obtenerProducto(productoId);
        double subtotal = producto.precio() * cantidad;
        double total = subtotal * (1 + igv / 100.0);
        return Map.of(
                "producto", producto,
                "cantidad", cantidad,
                "subtotal", Math.round(subtotal * 100.0) / 100.0,
                "igvAplicado", igv + "%",
                "total", Math.round(total * 100.0) / 100.0
        );
    }
}
