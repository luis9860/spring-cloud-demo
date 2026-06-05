package com.cibertec.pedido.config;

import com.cibertec.pedido.domain.CodigoMesaSilla;
import com.cibertec.pedido.domain.Estados;
import com.cibertec.pedido.entity.Mesa;
import com.cibertec.pedido.entity.Silla;
import com.cibertec.pedido.repository.MesaRepository;
import com.cibertec.pedido.repository.SillaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PedidoDataLoader {

    @Bean
    CommandLineRunner init(MesaRepository mesaRepository, SillaRepository sillaRepository) {
        return args -> {
            if (mesaRepository.count() > 0) {
                return;
            }
            for (int i = 1; i <= 5; i++) {
                String codigo = CodigoMesaSilla.codigoMesaPorNumero(i);
                Mesa m = new Mesa();
                m.setRestauranteId(1L);
                m.setNumero(i);
                m.setCodigo(codigo);
                m.setCapacidadSillas(4);
                m.setEstado(Estados.MESA_LIBRE);
                m.setQrToken("mesa-demo-" + i);
                mesaRepository.save(m);

                for (int s = 1; s <= 4; s++) {
                    Silla silla = new Silla();
                    silla.setMesaId(m.getId());
                    silla.setNumero(s);
                    silla.setCodigo(CodigoMesaSilla.codigoSilla(codigo, s));
                    sillaRepository.save(silla);
                }
            }
        };
    }
}
