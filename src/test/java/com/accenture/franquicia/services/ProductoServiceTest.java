package com.accenture.franquicia.services;

import com.accenture.franquicia.model.Franquicia;
import com.accenture.franquicia.model.Producto;
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
class ProductoServiceTest {

    @Mock private FranquiciaRepository franquiciaRepository;
    @Mock private SucursalRepository sucursalRepository;
    @Mock private ProductoRepository productoRepository;

    @InjectMocks
    private FranquiciaService franquiciaService;

    private Sucursal sucursal;
    private Producto producto;

    @BeforeEach
    void setUp() {
        sucursal = Sucursal.builder().id(10L).name("Sucursal Norte").franquiciaId(1L).build();
        producto = Producto.builder()
                .id(100L)
                .name("Producto A")
                .stock(50)
                .sucursalId(10L)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── createProducto ──────────────────────────────────────

    @Test
    void createProducto_nuevo_exitoso() {
        when(sucursalRepository.findById(10L)).thenReturn(Mono.just(sucursal));
        when(productoRepository.findByNameAndSucursalId("Producto A", 10L)).thenReturn(Mono.empty());
        when(productoRepository.save(any(Producto.class))).thenReturn(Mono.just(producto));

        StepVerifier.create(franquiciaService.createProducto(10L, producto))
                .expectNextMatches(p -> p.getName().equals("Producto A") && p.getStock() == 50)
                .verifyComplete();
    }

    @Test
    void createProducto_existente_acumulaStock() {
        Producto existente = Producto.builder().id(100L).name("Producto A").stock(30).sucursalId(10L).build();
        Producto actualizado = Producto.builder().id(100L).name("Producto A").stock(80).sucursalId(10L).build();

        when(sucursalRepository.findById(10L)).thenReturn(Mono.just(sucursal));
        when(productoRepository.findByNameAndSucursalId("Producto A", 10L)).thenReturn(Mono.just(existente));
        when(productoRepository.save(any(Producto.class))).thenReturn(Mono.just(actualizado));

        Producto nuevoStock = Producto.builder().name("Producto A").stock(50).build();

        StepVerifier.create(franquiciaService.createProducto(10L, nuevoStock))
                .expectNextMatches(p -> p.getStock() == 80)
                .verifyComplete();
    }

    @Test
    void createProducto_nombreVacio_retornaError() {
        Producto sinNombre = Producto.builder().name("").stock(10).build();

        StepVerifier.create(franquiciaService.createProducto(10L, sinNombre))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().contains("no puede estar vacío"))
                .verify();
    }

    @Test
    void createProducto_stockNegativo_retornaError() {
        Producto stockNegativo = Producto.builder().name("Producto B").stock(-1).build();

        StepVerifier.create(franquiciaService.createProducto(10L, stockNegativo))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().contains("negativo"))
                .verify();
    }

    @Test
    void createProducto_sucursalNoExiste_retornaError() {
        when(sucursalRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(franquiciaService.createProducto(99L, producto))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("99"))
                .verify();
    }

    // ── getProductoById ──────────────────────────────────────

    @Test
    void getProductoById_encontrado() {
        when(productoRepository.findById(100L)).thenReturn(Mono.just(producto));

        StepVerifier.create(franquiciaService.getProductoById(100L))
                .expectNextMatches(p -> p.getId().equals(100L))
                .verifyComplete();
    }

    @Test
    void getProductoById_noEncontrado_retornaError() {
        when(productoRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(franquiciaService.getProductoById(99L))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("99"))
                .verify();
    }

    // ── updateStock ──────────────────────────────────────

    @Test
    void updateStock_exitoso() {
        Producto actualizado = Producto.builder().id(100L).name("Producto A").stock(200).build();
        when(productoRepository.findById(100L)).thenReturn(Mono.just(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(Mono.just(actualizado));

        StepVerifier.create(franquiciaService.updateStock(100L, 200))
                .expectNextMatches(p -> p.getStock() == 200)
                .verifyComplete();
    }

    @Test
    void updateStock_negativo_retornaError() {
        StepVerifier.create(franquiciaService.updateStock(100L, -5))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("negativo"))
                .verify();
    }

    @Test
    void updateStock_noEncontrado_retornaError() {
        when(productoRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(franquiciaService.updateStock(99L, 10))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("99"))
                .verify();
    }

    // ── deleteProducto ──────────────────────────────────────

    @Test
    void deleteProducto_exitoso() {
        when(productoRepository.findById(100L)).thenReturn(Mono.just(producto));
        when(productoRepository.delete(any(Producto.class))).thenReturn(Mono.empty());

        StepVerifier.create(franquiciaService.deleteProducto(10L, 100L))
                .verifyComplete();
    }

    @Test
    void deleteProducto_sucursalIncorrecta_retornaError() {
        when(productoRepository.findById(100L)).thenReturn(Mono.just(producto));

        StepVerifier.create(franquiciaService.deleteProducto(99L, 100L))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("no pertenece"))
                .verify();
    }

    @Test
    void deleteProducto_noEncontrado_retornaError() {
        when(productoRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(franquiciaService.deleteProducto(10L, 99L))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("99"))
                .verify();
    }

    // ── getTopProductsByFranquiciaId ──────────────────────────────────────

    @Test
    void getTopProducts_retornaProductoConMasStock() {
        Franquicia franquicia = Franquicia.builder().id(1L).name("Franquicia Test").build();
        Producto p1 = Producto.builder().id(1L).name("A").stock(100).sucursalId(10L).build();
        Producto p2 = Producto.builder().id(2L).name("B").stock(50).sucursalId(10L).build();

        when(franquiciaRepository.findById(1L)).thenReturn(Mono.just(franquicia));
        when(sucursalRepository.findByFranquiciaId(1L)).thenReturn(Flux.just(sucursal));
        when(productoRepository.findBySucursalId(10L)).thenReturn(Flux.just(p1, p2));

        StepVerifier.create(franquiciaService.getTopProductsByFranquiciaId(1L))
                .expectNextMatches(dto -> dto.getProductName().equals("A") && dto.getStock() == 100)
                .verifyComplete();
    }

    @Test
    void getTopProducts_sucursalSinProductos_retornaDefault() {
        Franquicia franquicia = Franquicia.builder().id(1L).name("Franquicia Test").build();

        when(franquiciaRepository.findById(1L)).thenReturn(Mono.just(franquicia));
        when(sucursalRepository.findByFranquiciaId(1L)).thenReturn(Flux.just(sucursal));
        when(productoRepository.findBySucursalId(10L)).thenReturn(Flux.empty());

        StepVerifier.create(franquiciaService.getTopProductsByFranquiciaId(1L))
                .expectNextMatches(dto -> dto.getProductName().equals("No hay productos"))
                .verifyComplete();
    }

    @Test
    void getTopProducts_franquiciaNoExiste_retornaError() {
        when(franquiciaRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(franquiciaService.getTopProductsByFranquiciaId(99L))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("99"))
                .verify();
    }
}
