package com.cibertec.producto;

import com.cibertec.producto.dto.CrearProductoRequest;
import com.cibertec.producto.dto.ProductoDto;
import com.cibertec.producto.entity.Categoria;
import com.cibertec.producto.entity.ProductoEntity;
import com.cibertec.producto.repository.CategoriaRepository;
import com.cibertec.producto.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@RefreshScope
@RestController
@RequestMapping("/productos")
public class ProductoController {

    @Value("${producto.descuento:0}")
    private int descuento;

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoController(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    public List<ProductoDto> listar(
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId,
            @RequestParam(required = false) String estacion,
            @RequestParam(required = false) Long categoriaId) {
        List<ProductoEntity> lista;
        if (estacion != null && !estacion.isBlank()) {
            lista = productoRepository.findByRestauranteIdAndActivoTrueAndEstacion(restauranteId, estacion);
        } else if (categoriaId != null) {
            lista = productoRepository.findByRestauranteIdAndActivoTrueAndCategoriaId(restauranteId, categoriaId);
        } else {
            lista = productoRepository.findByRestauranteIdAndActivoTrue(restauranteId);
        }
        return lista.stream().map(this::toDto).toList();
    }

    @GetMapping("/categorias")
    public List<Map<String, Object>> categorias(
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return categoriaRepository.findByRestauranteId(restauranteId).stream()
                .map(c -> Map.<String, Object>of("id", c.getId(), "nombre", c.getNombre()))
                .toList();
    }

    /** Respuesta simple para Feign (pedido-service /simular). */
    @GetMapping("/{id}")
    public Producto obtener(@PathVariable Long id,
                            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        ProductoEntity entity = productoRepository.findByIdAndRestauranteIdAndActivoTrue(id, restauranteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado: " + id));
        double precio = aplicarDescuento(entity.getPrecio());
        return new Producto(entity.getId(), entity.getNombre(), precio, entity.getEstacion());
    }

    @PostMapping
    public ProductoDto crear(
            @RequestBody CrearProductoRequest request,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        ProductoEntity p = new ProductoEntity();
        p.setRestauranteId(restauranteId);
        p.setNombre(request.nombre());
        p.setPrecio(request.precio());
        p.setCategoriaId(request.categoriaId());
        p.setEstacion(request.estacion() != null ? request.estacion() : "COCINA");
        p.setActivo(true);
        productoRepository.save(p);
        return toDto(p);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id,
                           @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        ProductoEntity p = productoRepository.findByIdAndRestauranteIdAndActivoTrue(id, restauranteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        p.setActivo(false);
        productoRepository.save(p);
    }

    private ProductoDto toDto(ProductoEntity e) {
        return new ProductoDto(e.getId(), e.getNombre(), e.getPrecio(), e.getCategoriaId(), e.getEstacion());
    }

    private double aplicarDescuento(BigDecimal precio) {
        double base = precio.doubleValue();
        return BigDecimal.valueOf(base * (1 - descuento / 100.0))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
