package com.accenture.franquicia.repository;

import com.accenture.franquicia.model.Producto;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
public interface ProductoRepository extends ReactiveCrudRepository<Producto, Long> {
    
    Flux<Producto> findBySucursalId(long sucursalId);
}
