package com.accenture.franquicia.repository;

import com.accenture.franquicia.model.Sucursal;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface SucursalRepository extends ReactiveCrudRepository<Sucursal, Long> {
    
   Flux<Sucursal> findByFranquiciaId(long franquiciaId);
}
