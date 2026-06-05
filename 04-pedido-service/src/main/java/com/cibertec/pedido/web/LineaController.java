package com.cibertec.pedido.web;

import com.cibertec.pedido.dto.ActualizarEstadoLineaRequest;
import com.cibertec.pedido.service.ComandaService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/lineas")
public class LineaController {

    private final ComandaService comandaService;

    public LineaController(ComandaService comandaService) {
        this.comandaService = comandaService;
    }

    @PatchMapping("/{lineaId}/estado")
    public Map<String, Object> actualizarEstado(
            @PathVariable Long lineaId,
            @RequestBody ActualizarEstadoLineaRequest request,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return comandaService.actualizarEstadoLinea(lineaId, request.estado(), restauranteId);
    }
}
