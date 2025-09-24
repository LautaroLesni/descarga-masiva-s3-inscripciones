package com.example.service;

import com.example.dto.AmazonGetFileDTO;
import com.example.entity.RepositorioArchivo;
import com.example.repository.RepositorioArchivoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class RepositorioArchivoService {

    protected final Logger logger = Logger.getLogger(getClass());

    @Inject
    RepositorioArchivoRepository repositorioArchivoRepository;

    public List<RepositorioArchivo> searchFile(AmazonGetFileDTO amazonGetFileDto){
        List<RepositorioArchivo> repositorioArchivoOptional  = repositorioArchivoRepository.findRepositorioArchivoByCuilInscripcionInstancia(amazonGetFileDto.getCuil(), amazonGetFileDto.getInscripcion(), amazonGetFileDto.getInstancia(), amazonGetFileDto.getFileName());
        if(!repositorioArchivoOptional.isEmpty()){
            return repositorioArchivoOptional;
        }else{
            return null;
        }
    }

    public List<RepositorioArchivo> searchFilesByInstanciaSedeId(Long instanciaSedeId){
        List<RepositorioArchivo> repositorioArchivoOptional  = repositorioArchivoRepository.findRepositorioArchivoByInstanciaSedeId(instanciaSedeId);
        if(!repositorioArchivoOptional.isEmpty()){
            return repositorioArchivoOptional;
        }else{
            return null;
        }
    }
}
