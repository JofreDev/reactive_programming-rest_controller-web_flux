package com.app.reactive.models.dao;

import com.app.reactive.models.documents.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String> {

    public Mono<Categoria> findByNombre(String nombre);



}
