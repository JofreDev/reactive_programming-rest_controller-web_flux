package com.app.reactive;

import com.app.reactive.models.documents.Categoria;
import com.app.reactive.models.documents.Producto;
import com.app.reactive.models.services.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import reactor.core.publisher.Flux;

import java.util.Date;

@SpringBootApplication
@EnableMongoRepositories // Importante !!
public class ReactiveApplication implements CommandLineRunner {

	private final ProductoService productoService;


	private final ReactiveMongoTemplate mongoTemplate;

	private static final Logger log = LoggerFactory.getLogger(ReactiveApplication.class);

    public ReactiveApplication(ProductoService productoService, ReactiveMongoTemplate mongoTemplate) {
        this.productoService = productoService;
        this.mongoTemplate = mongoTemplate;
    }

    public static void main(String[] args) {
		SpringApplication.run(ReactiveApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		mongoTemplate.dropCollection("productos").subscribe(); // Obligatoria las suscripción !!
		mongoTemplate.dropCollection("categorias").subscribe();

		Categoria electronico =  Categoria.builder().nombre("Electrónico").build();
		Categoria deporte =  Categoria.builder().nombre("Deporte").build();
		Categoria computacion =  Categoria.builder().nombre("Computación").build();
		Categoria muebles =  Categoria.builder().nombre("Muebles").build();

		Flux.just(electronico,deporte,computacion,muebles)
				.flatMap(productoService::save)
				.doOnNext( categoria -> log.info("Categoria :"+categoria.getNombre()+" guardada en BD"))
				// Incluir otro flujo con su logica una vez haya terminado el anterior
				// then se usa para Mono y theMany para Flux !!
				.thenMany(Flux.just(
						Producto.builder().categoria(electronico).nombre("TV Panasonic").precio(456.89).build(),
						Producto.builder().categoria(electronico).nombre("Sony Camara HD Digital").precio(177.89).build(),
						Producto.builder().categoria(electronico).nombre("Apple ipod").precio(46.89).build(),
						Producto.builder().categoria(computacion).nombre("Sony Notebook").precio(846.89).build(),
						Producto.builder().categoria(computacion).nombre("Hewlett Packard Multifuncional").precio(200.89).build(),
						Producto.builder().categoria(deporte).nombre("Bianchi Bicicleta").precio(70.89).build(),
						Producto.builder().categoria(computacion).nombre("HP Notebook Omen 17").precio(2500.89).build(),
						Producto.builder().categoria(muebles).nombre("Mica Cómoda 5 Cajones").precio(150.89).build(),
						Producto.builder().categoria(electronico).nombre("TV Sony Bravia OLED 4k Ultra HD").precio(2255.89).build()
						/* No sirve 'map' porque retornaria un Flux de Mono de producto.
						 * flatMap si nos retorna un Flux de productos nada más!!! */
				).flatMap(producto ->  {
					producto.setCreateAt(new Date());
					return productoService.save(producto);
				} ))

				.subscribe(producto -> log.info("Insert :" + producto.getId() +" "+ producto.getNombre()));

	}
}
