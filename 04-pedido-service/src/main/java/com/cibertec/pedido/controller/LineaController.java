package com.cibertec.pedido.controller;

import com.cibertec.pedido.dto.ActualizarEstadoLineaRequest;
import com.cibertec.pedido.service.ComandaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/lineas")
@Tag(name = "Líneas", description = "Estado de líneas de pedido")
public class LineaController {

    private final ComandaService comandaService;

    public LineaController(ComandaService comandaService) {
        this.comandaService = comandaService;
    }

    @PatchMapping("/{lineaId}/estado")
    @Operation(summary = "Actualizar estado de una línea")
    public Map<String, Object> actualizarEstado(
            @PathVariable Long lineaId,
            @RequestBody ActualizarEstadoLineaRequest request,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return comandaService.actualizarEstadoLinea(lineaId, request.estado(), restauranteId);
    }
}
