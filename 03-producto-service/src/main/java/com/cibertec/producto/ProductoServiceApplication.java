package com.cibertec.producto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de Productos.
 * Se registra automaticamente en Eureka (por tener el starter eureka-client)
 * y obtiene parte de su configuracion del Config Server.
 */
@SpringBootApplication
public class ProductoServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductoServiceApplication.class, args);
    }
}
