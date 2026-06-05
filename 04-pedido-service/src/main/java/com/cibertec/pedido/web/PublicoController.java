package com.cibertec.pedido.web;

import com.cibertec.pedido.dto.IdentificarComensalRequest;
import com.cibertec.pedido.dto.AgregarLineaRequest;
import com.cibertec.pedido.service.ComandaService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/publico")
public class PublicoController {

    private final ComandaService comandaService;

    public PublicoController(ComandaService comandaService) {
        this.comandaService = comandaService;
    }

    @GetMapping("/local")
    public Map<String, Object> local() {
        return comandaService.infoLocalPublico();
    }

    @GetMapping("/local/cola")
    public Map<String, Object> colaLocal() {
        return comandaService.colaLocalPublica();
    }

    @GetMapping("/mesa/{qrToken}")
    public Map<String, Object> mesa(@PathVariable String qrToken) {
        return comandaService.infoMesaPublica(qrToken);
    }

    @PostMapping("/pedido/{qrToken}/identificar")
    public Map<String, Object> identificar(
            @PathVariable String qrToken,
            @RequestBody IdentificarComensalRequest request) {
        return comandaService.identificarComensal(qrToken, request);
    }

    @GetMapping("/pedido/{qrToken}")
    public Map<String, Object> pedidoQr(
            @PathVariable String qrToken,
            @RequestParam(name = "mesaCodigo", required = false) String mesaCodigo,
            @RequestParam(name = "miSilla", required = false) String miSilla) {
        return comandaService.consultaPublicaQr(qrToken, mesaCodigo, miSilla);
    }

    @GetMapping("/pedido/{qrToken}/menu")
    public Map<String, Object> menu(
            @PathVariable String qrToken,
            @RequestParam(name = "mesaCodigo", required = false) String mesaCodigo) {
        return comandaService.menuPublico(qrToken, mesaCodigo);
    }

    @PostMapping("/pedido/{qrToken}/lineas")
    public Map<String, Object> agregarLinea(
            @PathVariable String qrToken,
            @RequestParam(name = "mesaCodigo", required = false) String mesaCodigo,
            @RequestParam(name = "miSilla") String miSilla,
            @RequestBody AgregarLineaRequest request) {
        return comandaService.agregarLineaPublica(qrToken, mesaCodigo, miSilla, request);
    }

    @PostMapping("/pedido/{qrToken}/enviar")
    public Map<String, Object> enviarPedido(
            @PathVariable String qrToken,
            @RequestParam(name = "mesaCodigo", required = false) String mesaCodigo,
            @RequestParam(name = "miSilla") String miSilla) {
        return comandaService.enviarPedidoPublico(qrToken, mesaCodigo, miSilla);
    }
}
