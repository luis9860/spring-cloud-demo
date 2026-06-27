package com.cibertec.producto.controller;

import com.cibertec.producto.Producto;
import com.cibertec.producto.dto.CrearProductoRequest;
import com.cibertec.producto.dto.ProductoDto;
import com.cibertec.producto.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/productos")
@Tag(name = "Productos", description = "Catálogo de productos del restaurante")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Listar productos activos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de productos")
    })
    public List<ProductoDto> listar(
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId,
            @RequestParam(required = false) String estacion,
            @RequestParam(required = false) Long categoriaId) {
        return productoService.listar(restauranteId, estacion, categoriaId);
    }

    @GetMapping("/categorias")
    @Operation(summary = "Listar categorías del restaurante")
    public List<Map<String, Object>> categorias(
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return productoService.categorias(restauranteId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID (Feign / simulación)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public Producto obtener(@PathVariable Long id,
                            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return productoService.obtener(id, restauranteId);
    }

    @PostMapping
    @Operation(summary = "Crear producto (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto creado"),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado")
    })
    public ProductoDto crear(
            @RequestBody CrearProductoRequest request,
            @RequestHeader(value = "X-Rol", defaultValue = "") String rol,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return productoService.crear(request, rol, restauranteId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto (ADMIN)")
    public ProductoDto actualizar(
            @PathVariable Long id,
            @RequestBody CrearProductoRequest request,
            @RequestHeader(value = "X-Rol", defaultValue = "") String rol,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return productoService.actualizar(id, request, rol, restauranteId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar producto (ADMIN)")
    @ApiResponse(responseCode = "204", description = "Producto desactivado")
    public void desactivar(@PathVariable Long id,
                           @RequestHeader(value = "X-Rol", defaultValue = "") String rol,
                           @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        productoService.desactivar(id, rol, restauranteId);
    }
}
