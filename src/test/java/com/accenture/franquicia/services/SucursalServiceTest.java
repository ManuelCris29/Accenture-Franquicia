package com.accenture.franquicia.services;

import com.accenture.franquicia.model.Franquicia;
import com.accenture.franquicia.model.Sucursal;
import com.accenture.franquicia.repository.FranquiciaRepository;
import com.accenture.franquicia.repository.SucursalRepository;
import com.accenture.franquicia.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SucursalServiceTest {

    @Mock private FranquiciaRepository franquiciaRepository;
    @Mock private SucursalRepository sucursalRepository;
    @Mock private ProductoRepository productoRepository;

    @InjectMocks
    private FranquiciaService franquiciaService;

    private Franquicia franquicia;
    private Sucursal sucursal;

    @BeforeEach
    void setUp() {
        franquicia = Franquicia.builder().id(1L).name("Franquicia Test").build();
        sucursal = Sucursal.builder()
                .id(10L)
                .name("Sucursal Norte")
                .franquiciaId(1L)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── createSucursal ──────────────────────────────────────

    @Test
    void createSucursal_exitoso() {
        when(franquiciaRepository.findById(1L)).thenReturn(Mono.just(franquicia));
        when(sucursalRepository.findByNameAndFranquiciaId("Sucursal Norte", 1L)).thenReturn(Mono.empty());
        when(sucursalRepository.save(any(Sucursal.class))).thenReturn(Mono.just(sucursal));

        StepVerifier.create(franquiciaService.createSucursal(1L, sucursal))
                .expectNextMatches(s -> s.getName().equals("Sucursal Norte"))
                .verifyComplete();
    }

    @Test
    void createSucursal_nombreVacio_retornaError() {
        Sucursal sinNombre = Sucursal.builder().name("").build();

        StepVerifier.create(franquiciaService.createSucursal(1L, sinNombre))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().contains("no puede estar vacío"))
                .verify();
    }

    @Test
    void createSucursal_franquiciaNoExiste_retornaError() {
        when(franquiciaRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(franquiciaService.createSucursal(99L, sucursal))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("99"))
                .verify();
    }

    @Test
    void createSucursal_nombreDuplicado_retornaError() {
        when(franquiciaRepository.findById(1L)).thenReturn(Mono.just(franquicia));
        when(sucursalRepository.findByNameAndFranquiciaId("Sucursal Norte", 1L))
                .thenReturn(Mono.just(sucursal));

        StepVerifier.create(franquiciaService.createSucursal(1L, sucursal))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().contains("Ya existe"))
                .verify();
    }

    // ── getSucursalById ──────────────────────────────────────

    @Test
    void getSucursalById_encontrada() {
        when(sucursalRepository.findById(10L)).thenReturn(Mono.just(sucursal));

        StepVerifier.create(franquiciaService.getSucursalById(10L))
                .expectNextMatches(s -> s.getId().equals(10L))
                .verifyComplete();
    }

    @Test
    void getSucursalById_noEncontrada_retornaError() {
        when(sucursalRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(franquiciaService.getSucursalById(99L))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("99"))
                .verify();
    }

    // ── getSucursalesByFranquicia ──────────────────────────────────────

    @Test
    void getSucursalesByFranquicia_retornaLista() {
        Sucursal s2 = Sucursal.builder().id(11L).name("Sucursal Sur").franquiciaId(1L).build();
        when(franquiciaRepository.findById(1L)).thenReturn(Mono.just(franquicia));
        when(sucursalRepository.findByFranquiciaId(1L)).thenReturn(Flux.just(sucursal, s2));

        StepVerifier.create(franquiciaService.getSucursalesByFranquicia(1L))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getSucursalesByFranquicia_franquiciaNoExiste_retornaError() {
        when(franquiciaRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(franquiciaService.getSucursalesByFranquicia(99L))
                .expectErrorMatches(e -> e instanceof RuntimeException)
                .verify();
    }

    // ── updateSucursalName ──────────────────────────────────────

    @Test
    void updateSucursalName_exitoso() {
        Sucursal actualizada = Sucursal.builder().id(10L).name("Nuevo Nombre").build();
        when(sucursalRepository.findById(10L)).thenReturn(Mono.just(sucursal));
        when(sucursalRepository.save(any(Sucursal.class))).thenReturn(Mono.just(actualizada));

        StepVerifier.create(franquiciaService.updateSucursalName(10L, "Nuevo Nombre"))
                .expectNextMatches(s -> s.getName().equals("Nuevo Nombre"))
                .verifyComplete();
    }

    @Test
    void updateSucursalName_nombreVacio_retornaError() {
        StepVerifier.create(franquiciaService.updateSucursalName(10L, ""))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException)
                .verify();
    }

    @Test
    void updateSucursalName_noEncontrada_retornaError() {
        when(sucursalRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(franquiciaService.updateSucursalName(99L, "Nuevo"))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("99"))
                .verify();
    }

    // ── deleteSucursal ──────────────────────────────────────

    @Test
    void deleteSucursal_exitoso() {
        when(sucursalRepository.findById(10L)).thenReturn(Mono.just(sucursal));
        when(productoRepository.findBySucursalId(10L)).thenReturn(Flux.empty());
        when(sucursalRepository.deleteById(10L)).thenReturn(Mono.empty());

        StepVerifier.create(franquiciaService.deleteSucursal(10L))
                .verifyComplete();
    }

    @Test
    void deleteSucursal_noEncontrada_retornaError() {
        when(sucursalRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(franquiciaService.deleteSucursal(99L))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("99"))
                .verify();
    }
}
