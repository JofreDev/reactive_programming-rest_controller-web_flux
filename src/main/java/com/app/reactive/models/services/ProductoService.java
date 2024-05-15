package com.app.reactive.models.services;


import com.app.reactive.models.documents.Categoria;
import com.app.reactive.models.documents.Producto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoService {

    public Flux<Producto> findAll();

    public Flux<Producto> findAllWithNameUpperCase();

    public Flux<Producto> findAllWithNameUpperCaseRepeat();

    public Mono<Producto> findById(String id);

    public Mono<Producto> save(Producto producto);
    public Mono<Void> delete(Producto producto);

    public Flux<Categoria> findAllCategoria();

    public Mono<Categoria> findCategoriaById(String id);

    public Mono<Categoria> save(Categoria categoria);
    public Mono<Producto> findByNombre(String nombre);

    public Mono<Categoria> findCategoriaByNombre(String nombre);

}
