package com.example.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AmazonSaveFileDTO implements Serializable {
    private String base64;
    private String filePath;
    private String fileName;
    private String cuil;
    private Long inscripcion;
    private Long instancia;
}