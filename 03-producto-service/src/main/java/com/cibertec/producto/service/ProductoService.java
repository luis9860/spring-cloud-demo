package com.cibertec.producto.service;

import com.cibertec.producto.Producto;
import com.cibertec.producto.dto.CrearProductoRequest;
import com.cibertec.producto.dto.ProductoDto;
import com.cibertec.producto.entity.ProductoEntity;
import com.cibertec.producto.repository.CategoriaRepository;
import com.cibertec.producto.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class ProductoService {

    @Value("${producto.descuento:0}")
    private int descuento;

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<ProductoDto> listar(Long restauranteId, String estacion, Long categoriaId) {
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

    public List<Map<String, Object>> categorias(Long restauranteId) {
        return categoriaRepository.findByRestauranteId(restauranteId).stream()
                .map(c -> Map.<String, Object>of("id", c.getId(), "nombre", c.getNombre()))
                .toList();
    }

    public Producto obtener(Long id, Long restauranteId) {
        ProductoEntity entity = productoRepository.findByIdAndRestauranteIdAndActivoTrue(id, restauranteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado: " + id));
        double precio = aplicarDescuento(entity.getPrecio());
        return new Producto(entity.getId(), entity.getNombre(), precio, entity.getEstacion());
    }

    public ProductoDto crear(CrearProductoRequest request, String rol, Long restauranteId) {
        requireAdmin(rol);
        validarProducto(request);
        ProductoEntity p = new ProductoEntity();
        p.setRestauranteId(restauranteId);
        p.setNombre(request.nombre().trim());
        p.setPrecio(request.precio());
        p.setCategoriaId(request.categoriaId());
        p.setEstacion(request.estacion() != null ? request.estacion() : "COCINA");
        p.setActivo(true);
        productoRepository.save(p);
        return toDto(p);
    }

    public ProductoDto actualizar(Long id, CrearProductoRequest request, String rol, Long restauranteId) {
        requireAdmin(rol);
        validarProducto(request);
        ProductoEntity p = productoRepository.findByIdAndRestauranteIdAndActivoTrue(id, restauranteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        p.setNombre(request.nombre().trim());
        p.setPrecio(request.precio());
        p.setCategoriaId(request.categoriaId());
        p.setEstacion(request.estacion() != null ? request.estacion() : "COCINA");
        productoRepository.save(p);
        return toDto(p);
    }

    public void desactivar(Long id, String rol, Long restauranteId) {
        requireAdmin(rol);
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

    private void requireAdmin(String rol) {
        if (!"ADMIN".equals(rol)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo ADMIN puede administrar productos");
        }
    }

    private void validarProducto(CrearProductoRequest request) {
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre de producto obligatorio");
        }
        if (request.precio() == null || request.precio().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Precio invalido");
        }
    }
}
