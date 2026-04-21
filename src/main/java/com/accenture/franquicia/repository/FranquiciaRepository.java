package com.accenture.franquicia.repository;

import com.accenture.franquicia.model.Franquicia;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
public interface FranquiciaRepository extends ReactiveCrudRepository<Franquicia, Long> {
    Mono<Franquicia> findByName(String name);
}
