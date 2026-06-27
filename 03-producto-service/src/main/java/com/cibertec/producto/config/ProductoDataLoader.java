package com.cibertec.producto.config;

import com.cibertec.producto.entity.Categoria;
import com.cibertec.producto.entity.ProductoEntity;
import com.cibertec.producto.repository.CategoriaRepository;
import com.cibertec.producto.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class ProductoDataLoader {

    @Bean
    CommandLineRunner init(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        return args -> {
            if (productoRepository.count() > 0) {
                return;
            }
            Categoria fondos = new Categoria();
            fondos.setRestauranteId(1L);
            fondos.setNombre("Fondos");
            categoriaRepository.save(fondos);

            Categoria bebidas = new Categoria();
            bebidas.setRestauranteId(1L);
            bebidas.setNombre("Bebidas");
            categoriaRepository.save(bebidas);

            guardar(productoRepository, 1L, "Lomo saltado", "28.50", fondos.getId(), "COCINA");
            guardar(productoRepository, 1L, "Arroz con pollo", "22.00", fondos.getId(), "COCINA");
            guardar(productoRepository, 1L, "Ceviche", "32.00", fondos.getId(), "COCINA");
            guardar(productoRepository, 1L, "Causa", "8.00", fondos.getId(), "COCINA");
            guardar(productoRepository, 1L, "Caldo de Gallina", "6.00", fondos.getId(), "COCINA");
            // IDs 1-3 compatibles con demo Feign / pedidos/simular
        };
    }

    private void guardar(ProductoRepository repo, Long restauranteId, String nombre,
                         String precio, Long categoriaId, String estacion) {
        ProductoEntity p = new ProductoEntity();
        p.setRestauranteId(restauranteId);
        p.setNombre(nombre);
        p.setPrecio(new BigDecimal(precio));
        p.setCategoriaId(categoriaId);
        p.setEstacion(estacion);
        p.setActivo(true);
        repo.save(p);
    }
}
