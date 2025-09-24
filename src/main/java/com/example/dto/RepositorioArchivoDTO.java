package com.example.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RepositorioArchivoDTO {

    private Long id;
    private String nombre;
    private String cuil;
    private String descripcion;
    private String metadata;
    private Long inscripcion;
    private Long instancia;
    private Integer estado;
}