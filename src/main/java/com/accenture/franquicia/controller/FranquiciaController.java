package com.accenture.franquicia.controller;

import com.accenture.franquicia.dto.*;
import com.accenture.franquicia.model.Franquicia;
import com.accenture.franquicia.model.Producto;
import com.accenture.franquicia.model.Sucursal;
import com.accenture.franquicia.services.FranquiciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/franquicias")
@RequiredArgsConstructor
public class FranquiciaController {

    private final FranquiciaService franquiciaService;

    // ── Franquicias ──────────────────────────────────────────────────────────

    @Tag(name = "Franquicias")
    @Operation(summary = "Crear franquicia")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<FranquiciaResponse> agregarFranquicia(@Valid @RequestBody FranquiciaRequest request) {
        Franquicia franquicia = new Franquicia();
        franquicia.setName(request.getName());
        return franquiciaService.createFranquicia(franquicia)
                .map(f -> FranquiciaResponse.builder()
                        .id(f.getId()).name(f.getName()).createdAt(f.getCreatedAt()).build());
    }

    @Tag(name = "Franquicias")
    @Operation(summary = "Listar todas las franquicias")
    @GetMapping
    public Flux<FranquiciaResponse> listarFranquicias() {
        return franquiciaService.getAllFranquicias()
                .map(f -> FranquiciaResponse.builder()
                        .id(f.getId()).name(f.getName()).createdAt(f.getCreatedAt()).build());
    }

    @Tag(name = "Franquicias")
    @Operation(summary = "Obtener franquicia por ID")
    @GetMapping("/{id}")
    public Mono<FranquiciaResponse> obtenerFranquiciaPorId(@PathVariable Long id) {
        return franquiciaService.getFranquiciaById(id)
                .map(f -> FranquiciaResponse.builder()
                        .id(f.getId()).name(f.getName()).createdAt(f.getCreatedAt()).build());
    }

    @Tag(name = "Franquicias")
    @Operation(summary = "Actualizar nombre de franquicia")
    @PatchMapping("/{id}/nombre")
    public Mono<FranquiciaResponse> actualizarNombreFranquicia(
            @PathVariable Long id,
            @RequestParam String nombre) {
        return franquiciaService.updateFranquicia(id, nombre)
                .map(f -> FranquiciaResponse.builder()
                        .id(f.getId()).name(f.getName()).createdAt(f.getCreatedAt()).build());
    }

    @Tag(name = "Franquicias")
    @Operation(summary = "Eliminar franquicia y sus sucursales/productos")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> eliminarFranquicia(@PathVariable Long id) {
        return franquiciaService.deleteFranquicia(id);
    }

    @Tag(name = "Franquicias")
    @Operation(summary = "Producto con más stock por sucursal de una franquicia")
    @GetMapping("/{id}/top-productos")
    public Flux<TopProductDTO> obtenerTopProductos(@PathVariable Long id) {
        return franquiciaService.getTopProductsByFranquiciaId(id);
    }

    // ── Sucursales ───────────────────────────────────────────────────────────

    @Tag(name = "Sucursales")
    @Operation(summary = "Agregar sucursal a una franquicia")
    @PostMapping("/{franquiciaId}/sucursales")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SucursalResponse> agregarSucursal(
            @PathVariable Long franquiciaId,
            @Valid @RequestBody SucursalRequest request) {
        Sucursal sucursal = new Sucursal();
        sucursal.setName(request.getName());
        return franquiciaService.createSucursal(franquiciaId, sucursal)
                .map(s -> SucursalResponse.builder()
                        .id(s.getId()).name(s.getName())
                        .franquiciaId(s.getFranquiciaId()).createdAt(s.getCreatedAt()).build());
    }

    @Tag(name = "Sucursales")
    @Operation(summary = "Listar sucursales de una franquicia")
    @GetMapping("/{franquiciaId}/sucursales")
    public Flux<SucursalResponse> listarSucursales(@PathVariable Long franquiciaId) {
        return franquiciaService.getSucursalesByFranquicia(franquiciaId)
                .map(s -> SucursalResponse.builder()
                        .id(s.getId()).name(s.getName())
                        .franquiciaId(s.getFranquiciaId()).createdAt(s.getCreatedAt()).build());
    }

    @Tag(name = "Sucursales")
    @Operation(summary = "Obtener sucursal por ID")
    @GetMapping("/sucursales/{sucursalId}")
    public Mono<SucursalResponse> obtenerSucursalPorId(@PathVariable Long sucursalId) {
        return franquiciaService.getSucursalById(sucursalId)
                .map(s -> SucursalResponse.builder()
                        .id(s.getId()).name(s.getName())
                        .franquiciaId(s.getFranquiciaId()).createdAt(s.getCreatedAt()).build());
    }

    @Tag(name = "Sucursales")
    @Operation(summary = "Actualizar nombre de sucursal")
    @PatchMapping("/sucursales/{sucursalId}/nombre")
    public Mono<SucursalResponse> actualizarNombreSucursal(
            @PathVariable Long sucursalId,
            @RequestParam String nombre) {
        return franquiciaService.updateSucursalName(sucursalId, nombre)
                .map(s -> SucursalResponse.builder()
                        .id(s.getId()).name(s.getName())
                        .franquiciaId(s.getFranquiciaId()).createdAt(s.getCreatedAt()).build());
    }

    @Tag(name = "Sucursales")
    @Operation(summary = "Eliminar sucursal y sus productos")
    @DeleteMapping("/sucursales/{sucursalId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> eliminarSucursal(@PathVariable Long sucursalId) {
        return franquiciaService.deleteSucursal(sucursalId);
    }

    // ── Productos ────────────────────────────────────────────────────────────

    @Tag(name = "Productos")
    @Operation(summary = "Agregar producto a una sucursal")
    @PostMapping("/sucursales/{sucursalId}/productos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ProductoResponse> agregarProducto(
            @PathVariable Long sucursalId,
            @Valid @RequestBody ProductoRequest request) {
        Producto producto = new Producto();
        producto.setName(request.getName());
        producto.setStock(request.getStock());
        return franquiciaService.createProducto(sucursalId, producto)
                .map(p -> ProductoResponse.builder()
                        .id(p.getId()).name(p.getName()).stock(p.getStock())
                        .sucursalId(p.getSucursalId()).createdAt(p.getCreatedAt()).build());
    }

    @Tag(name = "Productos")
    @Operation(summary = "Listar productos de una sucursal")
    @GetMapping("/sucursales/{sucursalId}/productos")
    public Flux<ProductoResponse> listarProductos(@PathVariable Long sucursalId) {
        return franquiciaService.getProductosBySucursal(sucursalId)
                .map(p -> ProductoResponse.builder()
                        .id(p.getId()).name(p.getName()).stock(p.getStock())
                        .sucursalId(p.getSucursalId()).createdAt(p.getCreatedAt()).build());
    }

    @Tag(name = "Productos")
    @Operation(summary = "Obtener producto por ID")
    @GetMapping("/sucursales/productos/{productoId}")
    public Mono<ProductoResponse> obtenerProductoPorId(@PathVariable Long productoId) {
        return franquiciaService.getProductoById(productoId)
                .map(p -> ProductoResponse.builder()
                        .id(p.getId()).name(p.getName()).stock(p.getStock())
                        .sucursalId(p.getSucursalId()).createdAt(p.getCreatedAt()).build());
    }

    @Tag(name = "Productos")
    @Operation(summary = "Actualizar stock de producto")
    @PatchMapping("/sucursales/productos/{productoId}/stock")
    public Mono<ProductoResponse> actualizarStock(
            @PathVariable Long productoId,
            @RequestParam @jakarta.validation.constraints.Min(value = 0, message = "El stock no puede ser negativo") Integer stock) {
        return franquiciaService.updateStock(productoId, stock)
                .map(p -> ProductoResponse.builder()
                        .id(p.getId()).name(p.getName()).stock(p.getStock())
                        .sucursalId(p.getSucursalId()).createdAt(p.getCreatedAt()).build());
    }

    @Tag(name = "Productos")
    @Operation(summary = "Actualizar nombre de producto")
    @PatchMapping("/sucursales/productos/{productoId}/nombre")
    public Mono<ProductoResponse> actualizarNombreProducto(
            @PathVariable Long productoId,
            @RequestParam String nombre) {
        return franquiciaService.UpdateProductoName(productoId, nombre)
                .map(p -> ProductoResponse.builder()
                        .id(p.getId()).name(p.getName()).stock(p.getStock())
                        .sucursalId(p.getSucursalId()).createdAt(p.getCreatedAt()).build());
    }

    @Tag(name = "Productos")
    @Operation(summary = "Eliminar producto de una sucursal")
    @DeleteMapping("/sucursales/{sucursalId}/productos/{productoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> eliminarProducto(
            @PathVariable Long sucursalId,
            @PathVariable Long productoId) {
        return franquiciaService.deleteProducto(sucursalId, productoId);
    }
}
