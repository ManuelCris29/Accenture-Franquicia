package com.accenture.franquicia.services;

import com.accenture.franquicia.dto.ErrorResponse;
import com.accenture.franquicia.dto.TopProductDTO;
import com.accenture.franquicia.model.Franquicia;
import com.accenture.franquicia.model.Producto;
import com.accenture.franquicia.model.Sucursal;
import com.accenture.franquicia.repository.FranquiciaRepository;
import com.accenture.franquicia.repository.ProductoRepository;
import com.accenture.franquicia.repository.SucursalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FranquiciaService {

    private final FranquiciaRepository franquiciaRepository;
    private final SucursalRepository sucursalRepository;
    private final ProductoRepository productoRepository;

    // ── Franquicia ──────────────────────────────────────────

    public Mono<Franquicia> createFranquicia(Franquicia franquicia) {
        franquicia.setCreated_at(LocalDateTime.now());
        return franquiciaRepository.save(franquicia)
                .doOnSuccess(f -> log.info("Franquicia creada: {}", f.getId()))
                ;
    }

    public Mono<Franquicia> getFranquiciaById(Long id) {
        return franquiciaRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Franquicia no encontrada con ID: " + id)))
                .doOnSuccess(f -> log.info("Franquicia obtenida: {}", f.getId()))
                ;
    }

    public Mono<Franquicia> updateFranquicia(Long id, String newName) {
        return franquiciaRepository.findById(id)
                .switchIfEmpty(Mono.error(
                    new RuntimeException("Franquicia no encontrada con ID: " + id)))
                .flatMap(existing -> {
                    existing.setName(newName);
                    return franquiciaRepository.save(existing);
                })
                .doOnSuccess(f -> log.info("Franquicia actualizada: {}", id))
                ;
    }

    public Flux <Franquicia> getAllFranquicias(){
        return franquiciaRepository.findAll();
    }

    // ── Sucursal ──────────────────────────────────────────

    public Mono<Sucursal> createSucursal(Long franquiciaId, Sucursal sucursal) {
        return franquiciaRepository.findById(franquiciaId)
                .switchIfEmpty(Mono.error
                    (new RuntimeException("Franquicia no encontrada con ID: " + franquiciaId)))
                .flatMap(f -> {
                    sucursal.setFranquiciaId(franquiciaId);
                    sucursal.setCreated_at(LocalDateTime.now());
                    return sucursalRepository.save(sucursal);
                })
                .doOnSuccess(s -> log.info("Sucursal creada: {}", s.getId(), franquiciaId))
                ;
    }

    public Mono<Sucursal> updateSucursalName(Long sucursalId, String newName) {
        return sucursalRepository.findById(sucursalId)
                .switchIfEmpty(Mono.error(
                    new RuntimeException("Sucursal no encontrada con ID: " + sucursalId)))
                .flatMap(existing -> {
                    existing.setName(newName);
                    return sucursalRepository.save(existing);
                })
                .doOnSuccess(s -> log.info("Sucursal actualizada: {}", sucursalId))
                ;
    }

     public Flux<Sucursal> getSucursalesByFranquiciaId(Long franquiciaId) {
        return sucursalRepository.findByFranquiciaId(franquiciaId);
    }

     // ── Producto ──────────────────────────────────────────

     public Mono<Producto> createProducto(Long sucursalId, Producto producto) {
        return sucursalRepository.findById(sucursalId)
                .switchIfEmpty(Mono.error(new RuntimeException("Sucursal no encontrada con ID: " + sucursalId)))
                .flatMap(s -> {
                    producto.setSucursalId(sucursalId);
                    producto.setCreated_at(LocalDateTime.now());
                    return productoRepository.save(producto);
                })
                .doOnSuccess(p -> log.info("Producto creado: {}", p.getId(), sucursalId));
    }

    public Mono <Void> deleteProducto(Long sucursalId, Long productoId){
        return productoRepository.findById(productoId)
                .switchIfEmpty(Mono.error(new RuntimeException("Producto no encontrado con ID: " + productoId)))
                .flatMap(p -> {
                    if (p.getSucursalId() != sucursalId) {
                        return Mono.error(new RuntimeException("El producto no pertenece a la sucursal especificada"));
                    }
                    return productoRepository.delete(p);
                })
                .doOnSuccess(v -> log.info("Producto eliminado: {}", productoId));
    }

    public Mono<Producto> updateStock(Long productoId, Integer newStock) {
        if (newStock<0) {
            return Mono.error(new RuntimeException("El stock no puede ser negativo"));
            
        }
        return productoRepository.findById(productoId)
                .switchIfEmpty(Mono.error(
                    new RuntimeException("Producto no encontrado con ID: " + productoId)))
                .flatMap(product -> {
                    product.setStock(newStock);
                    return productoRepository.save(product);
                })
                .doOnSuccess(p -> log.info("Stock actualizado para producto: {}", productoId));
            }
    public Mono<Producto> UpdateProductoName(Long productoId, String newName) {
        return productoRepository.findById(productoId)
                .switchIfEmpty(Mono.error(
                    new RuntimeException("Producto no encontrado con ID: " + productoId)))
                .flatMap(product -> {
                    product.setName(newName);
                    return productoRepository.save(product);
                })
                .doOnSuccess(p -> log.info("Nombre actualizado para producto: {}", productoId));
            }
    
   

    
}
