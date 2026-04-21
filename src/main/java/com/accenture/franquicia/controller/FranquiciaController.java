package com.accenture.franquicia.controller;

import com.accenture.franquicia.dto.TopProductDTO;
import com.accenture.franquicia.model.*;
import com.accenture.franquicia.services.FranquiciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/franquicias")
@RequiredArgsConstructor

public class FranquiciaController {

    private final FranquiciaService franquiciaService;

    //----- Franquicia ------------------------------------

    // Criterio 2: Agregar franquicia
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Franquicia> agregarFranquicia(@RequestBody Franquicia franquicia) {
        return franquiciaService.createFranquicia(franquicia);
    }

    // Criterio 3: Listar franquicias
    @GetMapping
    public Flux<Franquicia> listarFranquicias(){
        return franquiciaService.getAllFranquicias();
    }

    // Criterio 4: Obtener franquicia por id
    @GetMapping("/{id}")
    public Mono<Franquicia> obtenerFranquiciaPorId(@PathVariable Long id){
        return franquiciaService.getFranquiciaById(id);
    }

    // Plus Criterio 5: Actualizar nombre de franquicia
    @PatchMapping("/{id}/nombre")
    public Mono<Franquicia> actualizarNombreFranquicia(
        @PathVariable Long id, 
        @RequestParam String nombre) {
        return franquiciaService.updateFranquicia(id, nombre);
    }

    // Plus Criterio 6: Eliminar franquicia
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> eliminarFranquicia(@PathVariable Long id){
        return franquiciaService.deleteFranquicia(id);
    }

    // Listar sucursales de una franquicia
    @GetMapping("/{franquiciaId}/sucursales")
    public Flux<Sucursal> listarSucursales(@PathVariable Long franquiciaId) {
        return franquiciaService.getSucursalesByFranquicia(franquiciaId);
    }

    // Obtener sucursal por id
    @GetMapping("/sucursales/{sucursalId}")
    public Mono<Sucursal> obtenerSucursalPorId(@PathVariable Long sucursalId) {
        return franquiciaService.getSucursalById(sucursalId);
    }

    // Listar productos de una sucursal
    @GetMapping("/sucursales/{sucursalId}/productos")
    public Flux<Producto> listarProductos(@PathVariable Long sucursalId) {
        return franquiciaService.getProductosBySucursal(sucursalId);
    }


    // Obtener producto por id
    @GetMapping("/sucursales/productos/{productoId}")
    public Mono<Producto> obtenerProductoPorId(@PathVariable Long productoId) {
        return franquiciaService.getProductoById(productoId);
    }

    // Eliminar sucursal
    @DeleteMapping("/sucursales/{sucursalId}")
@   ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> eliminarSucursal(@PathVariable Long sucursalId) {
        return franquiciaService.deleteSucursal(sucursalId);
    }

    
    // Criterio 7: Producto con más stock por sucursal
    @GetMapping("/{id}/top-productos")
    public Flux<TopProductDTO> obtenerProductoPorSucursal(@PathVariable Long id){
        return franquiciaService.getTopProductsByFranquiciaId(id);
            
    }

    // Criterio 8: Agregar sucursal a franquicia
    @PostMapping("/{franquiciaId}/sucursales")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Sucursal> addBranch(
            @PathVariable Long franquiciaId,
            @RequestBody Sucursal sucursal) {
        return franquiciaService.createSucursal(franquiciaId, sucursal);
    }

    // Criterio 9: Actualizar nombre de sucursal
    @PatchMapping("/sucursales/{sucursalId}/nombre")
    public Mono<Sucursal> updateBranchName(
            @PathVariable Long sucursalId,
            @RequestParam String nombre) {
        return franquiciaService.updateSucursalName(sucursalId, nombre);
    }

    // Criterio 10: Agregarproducto a sucursal
    @PostMapping("/sucursales/{sucursalId}/productos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Producto> addProductToBranch(
            @PathVariable Long sucursalId,
            @RequestBody Producto producto) {
        return franquiciaService.createProducto(sucursalId, producto);
            }
    // Criterio 11: Eliminar producto de sucursal
    @DeleteMapping("/sucursales/{sucursalId}/productos/{productoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> removeProductFromBranch(
            @PathVariable Long sucursalId,
            @PathVariable Long productoId) {
        return franquiciaService.deleteProducto(sucursalId, productoId);
    }

    //Criterio 12: Modificar stock de producto
    @PatchMapping("/sucursales/productos/{productoId}/stock")
    public Mono<Producto> updateProductStock(
            @PathVariable Long productoId,
            @RequestBody Integer stock) {
        return franquiciaService.updateStock(productoId, stock);
    }

    // Plus criterio 13: Actualizar nombre de producto
    @PatchMapping("/sucursales/productos/{productId}/nombre")
    public Mono<Producto> updateProductName(
            @PathVariable Long productId,
            @RequestParam String nombre) {
        return franquiciaService.UpdateProductoName(productId, nombre);
    }



}