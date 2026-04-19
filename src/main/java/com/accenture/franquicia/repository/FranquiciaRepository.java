package com.accenture.franquicia.repository;

import com.accenture.franquicia.model.Franquicia;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface FranquiciaRepository extends ReactiveCrudRepository<Franquicia, Long> {
    
}
