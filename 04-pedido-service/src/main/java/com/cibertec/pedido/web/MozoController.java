package com.cibertec.pedido.web;

import com.cibertec.pedido.service.ComandaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mozo")
public class MozoController {

    private final ComandaService comandaService;

    public MozoController(ComandaService comandaService) {
        this.comandaService = comandaService;
    }

    @GetMapping("/notificaciones")
    public List<Map<String, Object>> notificaciones(
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return comandaService.notificacionesMozo(restauranteId);
    }

    @GetMapping("/platos-listos")
    public List<Map<String, Object>> platosListos(
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return comandaService.platosListosMozo(restauranteId);
    }
}
