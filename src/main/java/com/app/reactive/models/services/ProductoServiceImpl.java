package com.app.reactive.models.services;


import com.app.reactive.models.dao.CategoriaDao;
import com.app.reactive.models.dao.ProductoDao;
import com.app.reactive.models.documents.Categoria;
import com.app.reactive.models.documents.Producto;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class ProductoServiceImpl implements ProductoService{

    private final ProductoDao productoDao;

    private final CategoriaDao categoriaDao;

    private static final Logger log = LoggerFactory.getLogger(ProductoServiceImpl.class);

    @Override
    public Flux<Producto> findAll() {
        return productoDao.findAll();
    }

    @Override
    public Flux<Producto> findAllWithNameUpperCase() {

        return productoDao.findAll()
                .map(producto ->  {
                    producto.setNombre(producto.getNombre().toUpperCase());
                    return  producto;
                });
    }

    @Override
    public Flux<Producto> findAllWithNameUpperCaseRepeat() {
        return findAllWithNameUpperCase().repeat(5000);
    }

    @Override
    public Mono<Producto> findById(String id) {
        return productoDao.findById(id);
    }

    @Override
    public Mono<Producto> save(Producto producto) {
        return productoDao.save(producto);
    }

    @Override
    public Mono<Void> delete(Producto producto) {
        return productoDao.delete(producto);
    }

    @Override
    public Flux<Categoria> findAllCategoria() {
        return categoriaDao.findAll();
    }

    @Override
    public Mono<Categoria> findCategoriaById(String id) {
        return categoriaDao.findById(id);
    }

    @Override
    public Mono<Categoria> save(Categoria categoria) {
        return categoriaDao.save(categoria);
    }
}
