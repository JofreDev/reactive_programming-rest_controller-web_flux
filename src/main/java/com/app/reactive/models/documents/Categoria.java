package com.app.reactive.models.documents;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "categorias")
public class Categoria {

    @Id
    @NotEmpty
    private String id;

    @NotEmpty
    private String nombre;

    public Categoria(String nombre) {
        this.nombre = nombre;
    }
}
