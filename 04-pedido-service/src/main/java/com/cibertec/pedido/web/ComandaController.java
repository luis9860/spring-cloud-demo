package com.cibertec.pedido.web;

import com.cibertec.pedido.service.ComandaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comandas")
public class ComandaController {

    private final ComandaService comandaService;

    public ComandaController(ComandaService comandaService) {
        this.comandaService = comandaService;
    }

    @PostMapping("/{comandaId}/enviar")
    public Map<String, Object> enviar(
            @PathVariable Long comandaId,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return comandaService.enviarComanda(comandaId, restauranteId);
    }

    @PostMapping("/{comandaId}/aceptar-cocina")
    public Map<String, Object> aceptarEnCocina(
            @PathVariable Long comandaId,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return comandaService.aceptarComandaEnCocina(comandaId, restauranteId);
    }

    @GetMapping("/pendientes")
    public List<Map<String, Object>> pendientes(@RequestParam(required = false) String estacion) {
        return comandaService.pendientesCocina(estacion);
    }

    @PatchMapping("/{comandaId}/entregada")
    public Map<String, Object> entregada(
            @PathVariable Long comandaId,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return comandaService.marcarEntregada(comandaId, restauranteId);
    }
}
