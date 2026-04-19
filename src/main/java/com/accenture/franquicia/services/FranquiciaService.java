package com.accenture.franquicia.services;

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

        if (franquicia.getName() == null || franquicia.getName().isBlank()) {
             return Mono.error(
                new IllegalArgumentException("El nombre de la franquicia no puede estar vacío"));
        }
        
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
       
        // En updateNombreFranquicia
        if (newName == null || newName.isBlank()) {
            return Mono.error(
                new IllegalArgumentException("El nombre no puede estar vacío"));
        }
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

    public Mono<Void> deleteFranquicia(Long id) {
        return franquiciaRepository.findById(id)
            .switchIfEmpty(Mono.error(
                new RuntimeException("Franquicia no encontrada: " + id)))
            .flatMap(franquicia ->
                sucursalRepository.findByFranquiciaId(id)
                    .flatMap(sucursal ->
                        productoRepository.findBySucursalId(sucursal.getId())
                            .flatMap(producto ->
                                productoRepository.deleteById(producto.getId())
                            )
                            .then(sucursalRepository.deleteById(sucursal.getId()))
                    )
                    .then(franquiciaRepository.deleteById(id))
            )
            .doOnSuccess(v -> log.info("Franquicia eliminada: {}", id));
    }

    // ── Sucursal ──────────────────────────────────────────

    public Mono<Sucursal> createSucursal(Long franquiciaId, Sucursal sucursal) {
        
        // En addSucursal
        if (sucursal.getName() == null || sucursal.getName().isBlank()) {
            return Mono.error(
                new IllegalArgumentException("El nombre de la sucursal no puede estar vacío"));
        }

        return franquiciaRepository.findById(franquiciaId)
                .switchIfEmpty(Mono.error
                    (new RuntimeException("Franquicia no encontrada con ID: " + franquiciaId)))
                .flatMap(f -> {
                    sucursal.setFranquiciaId(franquiciaId);
                    sucursal.setCreated_at(LocalDateTime.now());
                    return sucursalRepository.save(sucursal);
                })
                .doOnSuccess(s -> log.info("Sucursal creada: {}", 
                s.getId(), franquiciaId));
    }

    public Mono<Sucursal> getSucursalById(Long sucursalId) {
        return sucursalRepository.findById(sucursalId)
            .switchIfEmpty(Mono.error(
                new RuntimeException("Sucursal no encontrada: " + sucursalId)));
    }

    public Flux<Sucursal> getSucursalesByFranquicia(Long franquiciaId) {
        return franquiciaRepository.findById(franquiciaId)
            .switchIfEmpty(Mono.error(
                new RuntimeException("Franquicia no encontrada: " + franquiciaId)))
            .flatMapMany(f ->
                sucursalRepository.findByFranquiciaId(franquiciaId));
    }

    public Mono<Sucursal> updateSucursalName(Long sucursalId, String newName) {
        
        if (newName == null || newName.isBlank()) {
           return Mono.error(
            new IllegalArgumentException("El nombre no puede estar vacío"));
        }

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

    public Mono<Void> deleteSucursal(Long sucursalId) {
        return sucursalRepository.findById(sucursalId)
            .switchIfEmpty(Mono.error(
                new RuntimeException("Sucursal no encontrada: " + sucursalId)))
            .flatMap(sucursal ->
                productoRepository.findBySucursalId(sucursalId)
                    .flatMap(producto ->
                        productoRepository.deleteById(producto.getId())
                    )
                    .then(sucursalRepository.deleteById(sucursalId))
            )
            .doOnSuccess(v -> log.info("Sucursal eliminada: {}", sucursalId));
    }

     // ── Producto ──────────────────────────────────────────

     public Mono<Producto> createProducto(Long sucursalId, Producto producto) {

        if (producto.getName() == null || producto.getName().isBlank()) {
        return Mono.error(
            new IllegalArgumentException("El nombre del producto no puede estar vacío"));
        }
        if (producto.getStock() < 0) {
            return Mono.error(
                new IllegalArgumentException("El stock no puede ser negativo"));
        }

        return sucursalRepository.findById(sucursalId)
                .switchIfEmpty(Mono.error(new RuntimeException("Sucursal no encontrada con ID: " + sucursalId)))
                .flatMap(s -> {
                    producto.setSucursalId(sucursalId);
                    producto.setCreated_at(LocalDateTime.now());
                    return productoRepository.save(producto);
                })
                .doOnSuccess(p -> log.info("Producto creado: {}", p.getId(), sucursalId));
    }


    public Mono<Producto> getProductoById(Long productoId) {
        return productoRepository.findById(productoId)
            .switchIfEmpty(Mono.error(
                new RuntimeException("Producto no encontrado: " + productoId)));
    }

    public Flux<Producto> getProductosBySucursal(Long sucursalId) {
        return sucursalRepository.findById(sucursalId)
            .switchIfEmpty(Mono.error(
                new RuntimeException("Sucursal no encontrada: " + sucursalId)))
            .flatMapMany(s ->
                productoRepository.findBySucursalId(sucursalId));
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
        
        if (newName == null || newName.isBlank()) {
            return Mono.error(
        new IllegalArgumentException("El nombre no puede estar vacío"));
        }
        return productoRepository.findById(productoId)
                .switchIfEmpty(Mono.error(
                    new RuntimeException("Producto no encontrado con ID: " + productoId)))
                .flatMap(product -> {
                    product.setName(newName);
                    return productoRepository.save(product);
                })
                .doOnSuccess(p -> log.info("Nombre actualizado para producto: {}", productoId));
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

    //-─────────Productos con mas stock por sucursal────────────────────────────────────────

     public Flux<TopProductDTO> getTopProductsByFranquiciaId(Long franquiciaId) {
        return franquiciaRepository.findById(franquiciaId)
                .switchIfEmpty(Mono.error(
                    new RuntimeException("Franquicia no encontrada con ID: " + franquiciaId)))
                .flatMapMany(f -> 
                    sucursalRepository.findByFranquiciaId(franquiciaId)
                        .flatMap(sucursal -> 
                            sucursalRepository.findByFranquiciaId(franquiciaId)
                                .flatMap(s ->
                            productoRepository.findBySucursalId(sucursal.getId())
                                 .reduce((p1, p2) -> {
                                    Integer s1 = p1.getStock();
                                    Integer s2 = p2.getStock();
                                    if (s1 == null) return p2;
                                    if (s2 == null) return p1;
                                    return s1 >= s2 ? p1 : p2;
                                }) 
                                .map(topProducto -> TopProductDTO.builder()
                                        .sucursalId(sucursal.getId())
                                        .sucursalName(sucursal.getName())
                                        .productId(topProducto.getId())
                                        .productName(topProducto.getName())
                                        .stock(topProducto.getStock())
                                        .build()
                                    )

                            .defaultIfEmpty(TopProductDTO.builder()
                                .sucursalId(sucursal.getId())
                                .sucursalName(sucursal.getName())
                                .productId(0L)
                                .productName("No hay productos")
                                .stock(0)
                                .build()
                            )
                        )
                    )
                );
    }
    

}
