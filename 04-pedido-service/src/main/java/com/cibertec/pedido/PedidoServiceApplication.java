package com.cibertec.pedido;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Microservicio de Pedidos.
 * @EnableFeignClients activa los clientes Feign declarativos.
 * Llama a producto-service SIN conocer su IP/puerto: usa el nombre logico
 * registrado en Eureka + balanceo de carga del lado del cliente.
 */
@EnableFeignClients
@SpringBootApplication
public class PedidoServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PedidoServiceApplication.class, args);
    }
}
