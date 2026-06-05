package com.cibertec.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Servidor de Configuracion Centralizada (Spring Cloud Config).
 * @EnableConfigServer expone la configuracion de todos los microservicios
 * desde un unico lugar. Aqui usamos el perfil "native" (lee archivos del classpath);
 * en produccion lo normal es apuntar a un repositorio Git.
 */
@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
