package com.cibertec.pedido.controller;

import com.cibertec.pedido.service.ComandaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mozo")
@Tag(name = "Mozo", description = "Notificaciones y platos listos para mozo")
public class MozoController {

    private final ComandaService comandaService;

    public MozoController(ComandaService comandaService) {
        this.comandaService = comandaService;
    }

    @GetMapping("/notificaciones")
    @Operation(summary = "Notificaciones para el mozo")
    public List<Map<String, Object>> notificaciones(
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return comandaService.notificacionesMozo(restauranteId);
    }

    @GetMapping("/platos-listos")
    @Operation(summary = "Platos listos para servir")
    public List<Map<String, Object>> platosListos(
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return comandaService.platosListosMozo(restauranteId);
    }
}
