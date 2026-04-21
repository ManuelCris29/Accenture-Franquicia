package com.accenture.franquicia.services;

import com.accenture.franquicia.model.Franquicia;
import com.accenture.franquicia.repository.FranquiciaRepository;
import com.accenture.franquicia.repository.SucursalRepository;
import com.accenture.franquicia.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranquiciaServiceTest {

    @Mock private FranquiciaRepository franquiciaRepository;
    @Mock private SucursalRepository sucursalRepository;
    @Mock private ProductoRepository productoRepository;

    @InjectMocks
    private FranquiciaService franquiciaService;

    private Franquicia franquicia;

    @BeforeEach
    void setUp() {
        franquicia = Franquicia.builder()
                .id(1L)
                .name("Franquicia Test")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── createFranquicia ──────────────────────────────────────

    @Test
    void createFranquicia_exitoso() {
        when(franquiciaRepository.findByName("Franquicia Test")).thenReturn(Mono.empty());
        when(franquiciaRepository.save(any(Franquicia.class))).thenReturn(Mono.just(franquicia));

        StepVerifier.create(franquiciaService.createFranquicia(franquicia))
                .expectNextMatches(f -> f.getName().equals("Franquicia Test"))
                .verifyComplete();
    }

    @Test
    void createFranquicia_nombreVacio_retornaError() {
        Franquicia sinNombre = Franquicia.builder().name("").build();

        StepVerifier.create(franquiciaService.createFranquicia(sinNombre))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().contains("no puede estar vacío"))
                .verify();
    }

    @Test
    void createFranquicia_nombreNull_retornaError() {
        Franquicia sinNombre = Franquicia.builder().name(null).build();

        StepVerifier.create(franquiciaService.createFranquicia(sinNombre))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException)
                .verify();
    }

    @Test
    void createFranquicia_duplicado_retornaError() {
        when(franquiciaRepository.findByName("Franquicia Test")).thenReturn(Mono.just(franquicia));

        StepVerifier.create(franquiciaService.createFranquicia(franquicia))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().contains("Ya existe"))
                .verify();
    }

    // ── getFranquiciaById ──────────────────────────────────────

    @Test
    void getFranquiciaById_encontrada() {
        when(franquiciaRepository.findById(1L)).thenReturn(Mono.just(franquicia));

        StepVerifier.create(franquiciaService.getFranquiciaById(1L))
                .expectNextMatches(f -> f.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void getFranquiciaById_noEncontrada_retornaError() {
        when(franquiciaRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(franquiciaService.getFranquiciaById(99L))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("99"))
                .verify();
    }

    // ── updateFranquicia ──────────────────────────────────────

    @Test
    void updateFranquicia_exitoso() {
        Franquicia actualizada = Franquicia.builder().id(1L).name("Nuevo Nombre").build();
        when(franquiciaRepository.findById(1L)).thenReturn(Mono.just(franquicia));
        when(franquiciaRepository.save(any(Franquicia.class))).thenReturn(Mono.just(actualizada));

        StepVerifier.create(franquiciaService.updateFranquicia(1L, "Nuevo Nombre"))
                .expectNextMatches(f -> f.getName().equals("Nuevo Nombre"))
                .verifyComplete();
    }

    @Test
    void updateFranquicia_nombreVacio_retornaError() {
        StepVerifier.create(franquiciaService.updateFranquicia(1L, "  "))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException)
                .verify();
    }

    @Test
    void updateFranquicia_noEncontrada_retornaError() {
        when(franquiciaRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(franquiciaService.updateFranquicia(99L, "Nuevo"))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("99"))
                .verify();
    }

    // ── getAllFranquicias ──────────────────────────────────────

    @Test
    void getAllFranquicias_retornaLista() {
        Franquicia f2 = Franquicia.builder().id(2L).name("Otra").build();
        when(franquiciaRepository.findAll()).thenReturn(Flux.just(franquicia, f2));

        StepVerifier.create(franquiciaService.getAllFranquicias())
                .expectNextCount(2)
                .verifyComplete();
    }

    // ── deleteFranquicia ──────────────────────────────────────

    @Test
    void deleteFranquicia_exitoso() {
        when(franquiciaRepository.findById(1L)).thenReturn(Mono.just(franquicia));
        when(sucursalRepository.findByFranquiciaId(1L)).thenReturn(Flux.empty());
        when(franquiciaRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(franquiciaService.deleteFranquicia(1L))
                .verifyComplete();
    }

    @Test
    void deleteFranquicia_noEncontrada_retornaError() {
        when(franquiciaRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(franquiciaService.deleteFranquicia(99L))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("99"))
                .verify();
    }
}
