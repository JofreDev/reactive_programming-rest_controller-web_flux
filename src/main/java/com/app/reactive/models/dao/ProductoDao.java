package com.app.reactive.models.dao;


import com.app.reactive.models.documents.Producto;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/*Objetivo: Clase ProductDao para las operaciones de la BD,
*           podemos tener metodos personalizados, que hagan
*           consultas u operaciones.
*           ReactiveMongoRepository<Entidad, Tipo de dato del id>
*/
public interface ProductoDao extends ReactiveMongoRepository<Producto, String> {

    public Mono<Producto> findByNombre(String nombre);

    // Alternativa con @Query
    @Query("{'nombre':?0}")
    public Mono<Producto> obtenerPorNombre(String nombre);
}
