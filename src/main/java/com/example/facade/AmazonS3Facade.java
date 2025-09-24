package com.example.facade;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.example.dto.AmazonGetFileDTO;
import com.example.dto.FileResponseDTO;
import com.example.dto.RepositorioArchivoDTO;
import com.example.entity.RepositorioArchivo;
import com.example.service.AmazonS3Service;
import com.example.service.RepositorioArchivoService;
import com.example.service.integration.AmazonClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@ApplicationScoped
public class AmazonS3Facade {

    protected final Logger logger = Logger.getLogger(getClass());

    @Inject
    RepositorioArchivoService repositorioArchivoService;

    @Inject
    AmazonS3Service amazonS3Service;

    @Inject
    AmazonClient amazonClient;




    public List<RepositorioArchivoDTO> getFromAmazonS3(AmazonGetFileDTO amazonFileInfo) {
         return buildRepositorioArchivo(repositorioArchivoService.searchFile(amazonFileInfo));
    }
    public List<RepositorioArchivoDTO> buildRepositorioArchivo(List<RepositorioArchivo> fileResponse){
        List<RepositorioArchivoDTO> repositorioArchivoDTOList = new ArrayList<>();
        for (RepositorioArchivo repositorioArchivo : fileResponse) {
            repositorioArchivoDTOList.add(RepositorioArchivoDTO.builder()
                    .id(repositorioArchivo.getId())
                    .nombre(repositorioArchivo.getNombre())
                    .cuil(repositorioArchivo.getCuil())
                    .descripcion(repositorioArchivo.getDescripcion())
                    .metadata(repositorioArchivo.getMetadata())
                    .inscripcion(repositorioArchivo.getInscripcionId())
                    .instancia(repositorioArchivo.getInstanciaSedeId())
                    .estado(repositorioArchivo.getEstado()).build());
        }
        return repositorioArchivoDTOList;
    }

    public FileResponseDTO build(String fileName, String base64){
        FileResponseDTO fileResponse = new FileResponseDTO();
        fileResponse.setBase64(base64);
        fileResponse.setFileName(fileName);
        return fileResponse;
    }

    public FileResponseDTO downloadFile(String fileName) throws IOException{
        if(!fileName.equals("")){
            Path response = amazonS3Service.getFileResource(fileName);
            String base64 = amazonClient.convertResourceToBase64(response);
            FileResponseDTO fileResponseDTO = build(fileName, base64);
            return fileResponseDTO;
        } else {
            return null;
        }
    }

    public byte[] downloadAndZipFilesFromS3(List<RepositorioArchivo> files) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (RepositorioArchivo file : files) {
                try {
                    // Check if the S3 object exists
                    if (!amazonClient.doesObjectExist(file.getNombre())) {
                        logger.warn("Skipping missing S3 key: " + file.getNombre());
                        continue; // skip this file
                    }

                    // Stream the file directly from S3 into the ZIP
                    try (S3Object s3Object = amazonClient.getS3Object(file.getNombre());
                         InputStream s3InputStream = s3Object.getObjectContent()) {

                        ZipEntry entry = new ZipEntry(file.getCuil()+"_"+file.getMetadata());
                        zos.putNextEntry(entry);

                        // Transfer the contents directly to the ZIP
                        s3InputStream.transferTo(zos);
                        zos.closeEntry();
                    }

                } catch (AmazonS3Exception e) {
                    if ("NoSuchKey".equals(e.getErrorCode())) {
                        logger.warn("Skipping missing S3 key (caught exception): " + file.getNombre());
                    } else {
                        logger.error("Failed to download S3 file: " + file.getNombre(), e);
                    }
                } catch (IOException e) {
                    logger.error("Failed to add file to ZIP: " + file.getNombre(), e);
                }
            }

            zos.finish();
            return baos.toByteArray();

        } catch (IOException e) {
            logger.error("Error creating ZIP archive.", e);
            throw new RuntimeException("Failed to create ZIP file.", e);
        }
    }



}
