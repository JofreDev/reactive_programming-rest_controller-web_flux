package com.app.reactive;

import com.app.reactive.models.documents.Categoria;
import com.app.reactive.models.documents.Producto;
import com.app.reactive.models.services.ProductoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;

@AutoConfigureWebTestClient
/*No levanta todo un server para hacer las pruebas*/
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ReactiveApplicationTestsWithMocks {

	@Autowired
	private WebTestClient client;

	@Autowired
	private ProductoService productoService;

	@Value("${config.base.endpoint}")
	private String basePath;
	@Test
	void creaProductoTest() {

		Categoria categoria = productoService.findCategoriaByNombre("Muebles").block();

		client.post().uri(basePath)
				// Tipo de contenido en request
				.contentType(MediaType.APPLICATION_JSON)
				// Tipo de contenido en response
				.accept(MediaType.APPLICATION_JSON)
				// Va el objeto producto que queremos crear
				.body(Mono.just(Producto.builder()
						.nombre("Mesa comedor").precio(100.00).categoria(categoria).build()), Producto.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.producto.id").isNotEmpty()
				.jsonPath("$.producto.nombre").isEqualTo("Mesa comedor")
				.jsonPath("$.producto.categoria.nombre").isEqualTo("Muebles");
	}

	@Test
	void creaProducto2Test() {

		Categoria categoria = productoService.findCategoriaByNombre("Muebles").block();

		client.post().uri(basePath)
				// Tipo de contenido en request
				.contentType(MediaType.APPLICATION_JSON)
				// Tipo de contenido en response
				.accept(MediaType.APPLICATION_JSON)
				// Va el objeto producto que queremos crear
				.body(Mono.just(Producto.builder()
						.nombre("Mesa comedor").precio(100.00).categoria(categoria).build()), Producto.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {
				})
				.consumeWith(response -> {
					Object object = response.getResponseBody().get("producto");
					Producto p = new ObjectMapper().convertValue(object, Producto.class);
					Assertions.assertFalse(p.getId().isEmpty());
					Assertions.assertEquals("Mesa comedor",p.getNombre());
					Assertions.assertEquals("Muebles",p.getCategoria().getNombre());
				});
	}

}
