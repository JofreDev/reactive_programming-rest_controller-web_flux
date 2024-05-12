package com.app.reactive.models.documents;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/*Objetivo :  Mapeamos la colección de la bd de productos
*             por tal motivo usamos la API de mongo para spring.
*             La anotación convertirá la clase en formato BSON
*             (Binary JSON) que es el formato que usa mongo.
*               */
@Document(collection = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

   @Id // Es una llave
    private String id;

   // Atributos del documento
    @NotEmpty // de javax
    private String nombre;
    @NotNull
    private Double precio;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createAt;
    @Valid // Se va a validar
    @NotNull
    // No se ha validado aun qie la categoria exista !!
    private Categoria categoria;

    private String foto;

    public Producto(String nombre, Double precio) {
        super();
        this.nombre = nombre;
        this.precio = precio;
    }

    public Producto(String nombre, Double precio, Categoria categoria) {
        super();
        this.nombre = nombre;
        this.precio = precio;
        this.categoria = categoria;
    }
}
