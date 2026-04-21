package com.accenture.franquicia.repository;

import com.accenture.franquicia.model.Sucursal;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SucursalRepository extends ReactiveCrudRepository<Sucursal, Long> {
    
   Flux<Sucursal> findByFranquiciaId(long franquiciaId);
   Mono<Sucursal> findByNameAndFranquiciaId(String name, Long franquiciaId);

}
