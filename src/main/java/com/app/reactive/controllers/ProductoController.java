package com.app.reactive.controllers;
import com.app.reactive.models.documents.Producto;
import com.app.reactive.models.services.ProductoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;

    private final String ruta;

    public ProductoController(ProductoService productoService, @Value("${configuration.path}")String ruta) {
        this.productoService = productoService;
        this.ruta = ruta;
    }

    @PostMapping("/v2") // Desde Json usariamos Body/form-data y llenar los key-value correspondientes
    public Mono<ResponseEntity<Producto>> crearConFoto(Producto producto,@RequestPart FilePart file){

        if(producto.getCreateAt()==null)
            producto.setCreateAt(new Date());

        producto.setFoto(UUID.randomUUID().toString()+ "-"+file.filename()
                .replace(" ","")
                .replace(":","")
                .replace("\\",""));

        return file.transferTo(new File(ruta+producto.getFoto()))
                .then(productoService.save(producto)).map( producto1 -> ResponseEntity.created(
                        URI.create("/api/productos".concat(producto1.getId()))) // Estado created '201'
                .contentType(MediaType.APPLICATION_JSON)
                .body(producto1)
        );


    }

    @PostMapping("/upload/{id}")
    public Mono<ResponseEntity<Producto>> cargar(@PathVariable String id, @RequestPart FilePart file){
        return productoService.findById(id)
                .flatMap(p -> {
                    p.setFoto(UUID.randomUUID().toString()+ "-"+file.filename()
                            .replace(" ","")
                            .replace(":","")
                            .replace("\\",""));
                    return file.transferTo(new File(ruta+p.getFoto()))
                            .then(productoService.save(p));
                }).map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build()); // Rta sin contenido !;
    }

    /* A continuación se muestra 2 formas de definir endpoint rest
    * Primera : Es la más sencilla y devolvemos el flux
    * Segunda : Retornar un Mono del ResponseEntity.ResponseEntity no es reactivo por defecto
    *           Esta forma es más manual*/

    // Primeria
    /*
    @GetMapping
    public Flux<Producto> listar(){
        return productoService.findAll(); // Retorna el flux que se guarda en el response body
    }
*/
    //Segunda

    @GetMapping
    public Mono<ResponseEntity<Flux<Producto>>>  listar2(){
        //return Mono.just(ResponseEntity.ok(productoService.findAll())); // Respuesta con un 200 y guardamos en el body rta
        // De esta manera podemos ser mucho más especificos
        return Mono.just(
                ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(productoService.findAll())
        );
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Producto>> ver(@PathVariable String id){
        /*findById retorna un Mono<Producto> por defecto y requerimos convertir este
        * Observable en un Mono<ResponseEntity>

        * Importante !!! En este caso usamos 'map' porque debemos devolver un
        * Mono<ResponseEntity<Producto>> y no un Mono<Producto> y se modifica cada elemnto del
        * observable. En este caso en un Mono.*/

        return productoService.findById(id)
                // En este caso seguimos trabajando con el Mono y lo modificamos
                .map( producto -> {
                    return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(producto)
                            ;
                }).defaultIfEmpty(ResponseEntity.notFound().build()); // Rta sin contenido !
    }

    /* Solo a modo de ejemplo, asi se veria el metodo 'ver' con flatMap :
    *
    *  public Mono<ResponseEntity<Producto>> ver2(@PathVariable String id) {
        return productoService.findById(id)
                // En este caso creamos un nuevo Mono a partir de 'Producto' que se extrajo de Mono<Producto>
                .flatMap(producto -> Mono.just(ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(producto)));
    }
    * */

    // Con @Valid habilitamos la validación del objeto

    /* En el siguiente metodo se utiliza un retorno del tipo Mono<ResponseEntity<'Map<String,Object>>>'
    * con la finalidad de poder devolver ya sea un objeto Producto (creado) o el stack de errores que haya habido
    * en la ejecución. Por lo tanto al usar Map<String, Object> simulamos el construir un JSON a nuestro antojo
    * que al ser pasado al '.body()' de ResponseEntity se convierte como tal, dando la respuesta que querramos, es decir,
    * Mono<ResponseEntity<Map<"errores",List>>> ó Mono<ResponseEntity<Map<"producto",Producto>>>*/

    /* '@Valid @RequestBody  Mono<Producto>  monoProducto' -> Encapsulamos el objeto Producto en un Mono
    * con el fin de poder capturar el stack de errores que se pueda tener y devolverlo como rta*/
    @PostMapping
    public Mono<ResponseEntity<Map<String,Object>>> crear(@Valid @RequestBody  Mono<Producto>  monoProducto){

        // Map que contendrá la información y/o objetos que se requiera
        Map<String, Object> respuesta = new HashMap<String,Object>();
        respuesta.put("timestamp", new Date());
        /* Partimos el flujo desde el 'monoProducto para poder extraerlo del Mono' y se guarda en BD*/
        return monoProducto.flatMap( p -> {
            if(p.getCreateAt()==null)
                p.setCreateAt(new Date());
            return productoService.save(p);
            /* A continuación se usa un .map() para construir el objeto ResponseEntity<Map<String, Producto>>
            * por lo tanto se hace un .put al map de respuesta con los datos que se requieran, en este caso
            * el producto y un mensaje de rta satisfactorio. Hasta este punto no hay nada nuevo */
        }).map( producto -> {
                    respuesta.put("producto", producto);
                    respuesta.put("mensaje", "Producto Creado con Exito");
                    return ResponseEntity
                            .created(
                                    URI.create("/api/productos".concat(producto.getId()))) // Estado created '201'
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(respuesta);
                })
                /*Con este 'onErrorResume' en el flujo determinamos como reaccionar ante algun error capturado
                * en las partes anteriores del flujo.*/
                // Se toma el objeto Throwable (t)
                .onErrorResume(t -> {
                    // Se castea el Mono<Throwable> a Mono<WebExchangeBindException> para un uso más sencillo
                    return Mono.just(t).cast(WebExchangeBindException.class)
                            // Se convierte Mono<WebExchangeBindException> a un Mono<List<FieldError>> que contiene la lista de los campos con errores
                            .map(WebExchangeBindException::getFieldErrors)
                            /* Se convierte Mono<List<FieldError>> a un Flux<FieldError> con el fin de modificar facilmente elemento por elemento despues
                             .flatMapMany(Flux::fromIterable) convertir un Mono<List<Object>> a Flux<Object>*/
                            .flatMapMany(Flux::fromIterable)
                            // Se crea un nuevo Flux para que contenga Strings describiendo los errores 'Flux<String>'
                            // con el formato que se ve a continuación
                            .map(fieldError -> "El campo "+fieldError.getField() + " "+fieldError.getDefaultMessage())
                            // Se convierte el Flux a un Mono. Por lo tanto queda Mono<List<String>>
                            .collectList()
                            // se arma el Map<String,List<String>> para mostrar los errores
                            // posterior se arma el ResponseEntity con el Map
                            .flatMap( list -> {
                                respuesta.put("errors",list);
                                respuesta.put("status", HttpStatus.BAD_REQUEST.value());
                                return Mono.just(ResponseEntity.badRequest().body(respuesta));
                            });
                });
    }

    /* @RequestBody Producto producto -> Json del body de la petición
    * @PathVariable String id -> parametro del PUT
    * */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Producto>> editar (@RequestBody Producto producto, @PathVariable String id ){

        return productoService.findById(id)
                .flatMap( p -> {
                    p.setNombre(producto.getNombre());
                    p.setPrecio(producto.getPrecio());
                    p.setCategoria(producto.getCategoria());
                    return productoService.save(p); // acá estoy retornando un Mono<Producto> ||| Con .map esto no se podria hacer porque devolveria un Mono<Mono<Producto>>
                }).map(producto1 -> ResponseEntity.created(
                                URI.create("/api/productos".concat(producto1.getId()))) // Estado created '201'
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(producto1))
                .defaultIfEmpty(ResponseEntity.notFound().build()); // Cae acá si productoService.findById(id) retorna null


    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Object>> eliminar(@PathVariable String id){

        return productoService.findById(id)
                .flatMap(producto -> {
                    return productoService.delete(producto).then(Mono.just(ResponseEntity.noContent().build()));
                })// No se puede usar map porque El Mono retornado es de tipo void y no se hará efectiva la logica del map
                .defaultIfEmpty(ResponseEntity.notFound().build());

    }

    /* Alernativa usando 'Mono<Void>' */
    //@DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> eliminar2(@PathVariable String id){

        return productoService.findById(id)
                .flatMap(producto -> {
                    return productoService.delete(producto).then(Mono.just(new ResponseEntity<Void> (HttpStatus.NO_CONTENT)));
                })// No se puede usar map porque El Mono retornado es de tipo void y no se hará efectiva la logica del map
                .defaultIfEmpty(new ResponseEntity<Void> (HttpStatus.NOT_FOUND));

    }






}
