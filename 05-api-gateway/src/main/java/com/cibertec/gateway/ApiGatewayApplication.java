package com.cibertec.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway (puerta de entrada unica) con Spring Cloud Gateway.
 * Toda peticion externa entra por aqui (puerto 8080) y se enruta al
 * microservicio correspondiente, resuelto por nombre logico via Eureka (lb://).
 */
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
