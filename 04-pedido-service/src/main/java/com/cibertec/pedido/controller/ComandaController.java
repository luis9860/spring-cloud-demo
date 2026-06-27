package com.cibertec.pedido.controller;

import com.cibertec.pedido.service.ComandaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comandas")
@Tag(name = "Comandas", description = "Flujo de comandas hacia cocina")
public class ComandaController {

    private final ComandaService comandaService;

    public ComandaController(ComandaService comandaService) {
        this.comandaService = comandaService;
    }

    @PostMapping("/{comandaId}/enviar")
    @Operation(summary = "Enviar comanda a cocina")
    public Map<String, Object> enviar(
            @PathVariable Long comandaId,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return comandaService.enviarComanda(comandaId, restauranteId);
    }

    @PostMapping("/{comandaId}/aceptar-cocina")
    @Operation(summary = "Aceptar comanda en cocina")
    public Map<String, Object> aceptarEnCocina(
            @PathVariable Long comandaId,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return comandaService.aceptarComandaEnCocina(comandaId, restauranteId);
    }

    @GetMapping("/pendientes")
    @Operation(summary = "Cola de comandas pendientes en cocina")
    public List<Map<String, Object>> pendientes(@RequestParam(required = false) String estacion) {
        return comandaService.pendientesCocina(estacion);
    }

    @PatchMapping("/{comandaId}/entregada")
    @Operation(summary = "Marcar comanda como entregada al cliente")
    public Map<String, Object> entregada(
            @PathVariable Long comandaId,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return comandaService.marcarEntregada(comandaId, restauranteId);
    }
}
