package com.cibertec.pedido.controller;

import com.cibertec.pedido.dto.IdentificarComensalRequest;
import com.cibertec.pedido.dto.AgregarLineaRequest;
import com.cibertec.pedido.service.ComandaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/publico")
@Tag(name = "Público", description = "Flujo QR para comensales (sin JWT)")
public class PublicoController {

    private final ComandaService comandaService;

    public PublicoController(ComandaService comandaService) {
        this.comandaService = comandaService;
    }

    @GetMapping("/local")
    @Operation(summary = "Información del local")
    public Map<String, Object> local() {
        return comandaService.infoLocalPublico();
    }

    @GetMapping("/local/cola")
    @Operation(summary = "Cola pública del local")
    public Map<String, Object> colaLocal() {
        return comandaService.colaLocalPublica();
    }

    @GetMapping("/mesa/{qrToken}")
    @Operation(summary = "Info de mesa por token QR")
    public Map<String, Object> mesa(@PathVariable String qrToken) {
        return comandaService.infoMesaPublica(qrToken);
    }

    @PostMapping("/pedido/{qrToken}/identificar")
    @Operation(summary = "Identificar comensal en mesa")
    public Map<String, Object> identificar(
            @PathVariable String qrToken,
            @RequestBody IdentificarComensalRequest request) {
        return comandaService.identificarComensal(qrToken, request);
    }

    @GetMapping("/pedido/{qrToken}")
    @Operation(summary = "Consulta de pedido por QR")
    public Map<String, Object> pedidoQr(
            @PathVariable String qrToken,
            @RequestParam(name = "mesaCodigo", required = false) String mesaCodigo,
            @RequestParam(name = "miSilla", required = false) String miSilla) {
        return comandaService.consultaPublicaQr(qrToken, mesaCodigo, miSilla);
    }

    @GetMapping("/pedido/{qrToken}/menu")
    @Operation(summary = "Menú disponible vía QR")
    public Map<String, Object> menu(
            @PathVariable String qrToken,
            @RequestParam(name = "mesaCodigo", required = false) String mesaCodigo) {
        return comandaService.menuPublico(qrToken, mesaCodigo);
    }

    @PostMapping("/pedido/{qrToken}/lineas")
    @Operation(summary = "Agregar línea desde QR")
    public Map<String, Object> agregarLinea(
            @PathVariable String qrToken,
            @RequestParam(name = "mesaCodigo", required = false) String mesaCodigo,
            @RequestParam(name = "miSilla") String miSilla,
            @RequestBody AgregarLineaRequest request) {
        return comandaService.agregarLineaPublica(qrToken, mesaCodigo, miSilla, request);
    }

    @PostMapping("/pedido/{qrToken}/enviar")
    @Operation(summary = "Enviar pedido desde QR")
    public Map<String, Object> enviarPedido(
            @PathVariable String qrToken,
            @RequestParam(name = "mesaCodigo", required = false) String mesaCodigo,
            @RequestParam(name = "miSilla") String miSilla) {
        return comandaService.enviarPedidoPublico(qrToken, mesaCodigo, miSilla);
    }
}
