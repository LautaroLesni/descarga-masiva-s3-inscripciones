package com.example;

import com.example.dto.AmazonGetFileDTO;
import com.example.dto.FileResponseDTO;
import com.example.dto.RepositorioArchivoDTO;
import com.example.entity.RepositorioArchivo;
import com.example.facade.AmazonS3Facade;
import com.example.repository.RepositorioArchivoRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Path("/")
@Produces("application/zip")
public class DownloadResource {

    protected final Logger logger = Logger.getLogger(getClass());

    @Inject
    AmazonS3Facade amazonS3Facade;

    @Inject
    RepositorioArchivoRepository archivoRepository;

    @GET
    @Path("/instanciaSede/{id}")
    public List<RepositorioArchivo> getByInstanciaSedeId(@PathParam("id") Long id) {
        return archivoRepository.findRepositorioArchivoByInstanciaSedeId(id);
    }

    @GET
    @Path("/inscripcion/{id}")
    public List<RepositorioArchivo> getByInscripcionId(@PathParam("id") Long id) {
        return archivoRepository.findRepositorioArchivoByInscripcionId(id);
    }

    @GET
    @Path("/download/instanciaSede/{id}")
    @Produces("application/zip") // Change this line
    public Response downloadZipByInstanciaSedeId(@PathParam("id") Long id) {
        logger.info("======== downloadZipByInstanciaSedeId ========");

        List<RepositorioArchivo> filesToDownload = archivoRepository.findRepositorioArchivoByInstanciaSedeId(id);

        if (filesToDownload.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No files found for the provided 'instanciaSedeId'.")
                    .build();
        }

        // Call the facade to handle the S3 download and zip creation
        byte[] zipData = amazonS3Facade.downloadAndZipFilesFromS3(filesToDownload);

        // Build the response with the ZIP data and correct headers
        return Response.ok(zipData)
                .header("Content-Disposition", "attachment; filename=\"files.zip\"")
                .build();
    }

    // Add a similar method for 'inscripcionId'
    @GET
    @Path("/download/inscripcion/{id}")
    @Produces("application/zip") // Change this line
    public Response downloadZipByInscripcionId(@PathParam("id") Long id) {
        logger.info("======== downloadZipByInscripcionId ========");

        List<RepositorioArchivo> filesToDownload = archivoRepository.findRepositorioArchivoByInscripcionId(id);

        if (filesToDownload.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No files found for the provided 'inscripcionId'.")
                    .build();
        }

        byte[] zipData = amazonS3Facade.downloadAndZipFilesFromS3(filesToDownload);

        return Response.ok(zipData)
                .header("Content-Disposition", "attachment; filename=\"files.zip\"")
                .build();
    }

}