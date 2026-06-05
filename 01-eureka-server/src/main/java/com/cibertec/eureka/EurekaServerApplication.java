package com.cibertec.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Servidor de Descubrimiento (Service Discovery) con Netflix Eureka.
 * La anotacion @EnableEurekaServer convierte esta app en el "directorio telefonico"
 * donde los microservicios se registran y se buscan entre si.
 */
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
