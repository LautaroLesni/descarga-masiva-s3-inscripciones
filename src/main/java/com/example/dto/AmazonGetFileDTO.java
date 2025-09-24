package com.example.dto;

import lombok.Data;

@Data
public class AmazonGetFileDTO {
    private String cuil;
    private Long instancia;
    private Long inscripcion;
    private String fileName;
}
