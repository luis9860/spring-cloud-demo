package com.cibertec.pedido;

import com.cibertec.pedido.client.ProductoClient;
import com.cibertec.pedido.dto.Producto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final ProductoClient productoClient;

    @Value("${pedido.igv:18}")
    private int igv;   // valor centralizado desde Config Server (pedido-service.yml)

    public PedidoController(ProductoClient productoClient) {
        this.productoClient = productoClient;
    }

    /**
     * Simula la creacion de un pedido: consulta el producto en producto-service
     * (via Feign + Eureka) y calcula el total con IGV.
     */
    @GetMapping("/simular/{productoId}")
    public Map<String, Object> simular(@PathVariable Long productoId,
                                       @RequestParam(defaultValue = "1") int cantidad) {
        Producto producto = productoClient.obtenerProducto(productoId); // <-- llamada entre microservicios
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
