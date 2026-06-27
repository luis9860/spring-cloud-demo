package com.cibertec.pedido.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI pedidoOpenApi(@Value("${server.port:8082}") int port) {
        return new OpenAPI()
                .info(new Info()
                        .title("Pedido Service API")
                        .description("Mesas, comandas, cocina, mozo y flujo público QR")
                        .version("1.0.0")
                        .contact(new Contact().name("Cibertec DSW2")))
                .servers(List.of(new Server().url("http://localhost:" + port).description("Directo"),
                        new Server().url("http://localhost:8080/api").description("Vía API Gateway")));
    }
}
