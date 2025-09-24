package com.example.service.integration;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import net.coobird.thumbnailator.Thumbnails;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

@ApplicationScoped
@RegisterForReflection
public class AmazonClient {

    protected final Logger logger = Logger.getLogger(getClass());

    @ConfigProperty(name = "app.s3.endpoint")
    String s3Endpoint;

    @ConfigProperty(name = "app.s3.port")
    String s3Port;

    @ConfigProperty(name = "app.s3.accessKey")
    String s3AccessKey;

    @ConfigProperty(name = "app.s3.secretKey")
    String s3SecretKey;

    @ConfigProperty(name = "app.s3.bucketName")
    String s3BucketName;

    private volatile AmazonS3 s3client;

    public AmazonClient setS3Endpoint(String endpoint){
        s3Endpoint = endpoint;
        return this;
    }

    public AmazonClient setS3Port(String port){
        s3Port = port;
        return this;
    }

    public AmazonClient setS3AccessKey(String accessKey){
        s3AccessKey = accessKey;
        return this;
    }

    public AmazonClient setS3SecretKey(String secretKey){
        s3SecretKey =  secretKey;
        return this;
    }

    public AmazonClient setS3BucketName( String bucketName){
        s3BucketName = bucketName;
        return this;
    }

    private AmazonS3 s3Client() {
        // Check if the client is already initialized
        if (s3client == null) {
            // Synchronize to prevent multiple threads from creating the client at once
            synchronized (this) {
                // Re-check inside the synchronized block to handle race conditions
                if (s3client == null) {
                    // ... (your existing code to create the client) ...
                    AWSCredentials awsCredentials = new BasicAWSCredentials(s3AccessKey, s3SecretKey);
                    ClientConfiguration myClientConfig = new ClientConfiguration();
                    myClientConfig.setProtocol(Protocol.HTTPS);
                    System.setProperty("com.amazonaws.sdk.disableCertChecking", "true");

                    AmazonS3 client = new AmazonS3Client(awsCredentials, myClientConfig);
                    client.setEndpoint(s3Endpoint + ":" + s3Port);

                    // Assign the new client to the class field
                    this.s3client = client;
                }
            }
        }
        // Return the cached client
        return s3client;
    }

    public boolean doesObjectExist(String key) {
        return s3Client().doesObjectExist(s3BucketName, key);
    }

    public String uploadFile(File file, String codIdentificador) throws IOException {
        String fileName = generateFileName(file, codIdentificador);
        uploadFileTos3bucket(fileName, file);

        if (file.exists()) {
            file.delete();
        }

        return fileName;
    }

    private void uploadFileTos3bucket(String fileName, File file) {
        s3Client().putObject(s3BucketName, fileName, file);
    }

    public Path getResource(String path) throws IOException {
        InputStream inputStream = getS3ObjectInputStream(path).getDelegateStream();
        Path tempDir = Files.createTempDirectory("temp");
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        Path tempFilePath = tempDir.resolve(fileName);
        Files.copy(inputStream, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
        return tempFilePath;
    }

    private S3ObjectInputStream getS3ObjectInputStream(String path){
        return getS3Object(path).getObjectContent();
    }

    public S3Object getS3Object(String path) {
        return s3Client().getObject(s3BucketName, path);
    }

    private String generateFileName(File file, String id) {
        String originalFileName = file.getName();
        String uniqueFileName = id + "-" + originalFileName;
        return uniqueFileName;
    }

    public String convertResourceToBase64(Path resource) throws IOException {
        byte[] fileBytes = Files.readAllBytes(resource);
        return Base64.getEncoder().encodeToString(fileBytes);
    }

    public Boolean isImage(File file){
        try{
            ImageIO.read(file);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean fileLength(File file){
        logger.info("Calculando cantidad de MB");
        long fileSizeBytes = file.length();
        double fileSizeKB = fileSizeBytes / 1024.0;
        double fileSizeMB = fileSizeKB / 1024.0;
        logger.info("Pesa: " +fileSizeMB+" mb");
        if(fileSizeMB >= 5){
            return true;
        }
        return false;
    }

    public File compressImage(File inputFile, double quality) throws IOException {
        logger.info("Comprimiendo imagen");
        if (inputFile == null || !inputFile.exists() || inputFile.isDirectory()) {
            throw new IllegalArgumentException("El archivo de entrada no es válido.");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        logger.info("Optimizando imagen");

        Thumbnails.of(inputFile)
                .outputQuality(quality)
                .scale(1)
                .toOutputStream(baos);

        byte[] imageData = baos.toByteArray();
        String base64Image = Base64.getEncoder().encodeToString(imageData);
        File newFile = base64ToFileWithoutSplit(base64Image, inputFile.getName());

        long fileSizeBytes = newFile.length();
        double fileSizeKB = fileSizeBytes / 1024.0;
        double fileSizeMB = fileSizeKB / 1024.0;
        logger.info("Pesa con optimización: " +fileSizeMB+" mb");

        return newFile;
    }

    public File base64ToFile(String base64String, String fileName) {
        try {
            String[] base64Split = base64String.split(",");
            byte[] decodedBytes = Base64.getDecoder().decode(base64Split[1]);
            File file = new File(fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(decodedBytes);
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public File byteToFile(byte[] byteArray, String fileName) throws IOException{
        if (byteArray == null) {
            throw new IllegalArgumentException("El array de bytes no puede ser null");
        }
        File archivoTemp = null;
        try {
            logger.info("Nombre del archivo: " +fileName);
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex != -1 && dotIndex != fileName.length() - 1) {
                String name = fileName.substring(0, dotIndex);
                String extension = fileName.substring(dotIndex + 1);
                archivoTemp = new File(System.getProperty("java.io.tmpdir"), name + "." + extension);
            } else {
                System.err.println("El nombre del archivo no tiene una extensión válida");
            }
            // Escribir los bytes en el archivo temporal
            try (FileOutputStream fos = new FileOutputStream(archivoTemp)) {
                fos.write(byteArray);
            }

        } catch (IOException e) {
            System.err.println("Error al crear o escribir en el archivo temporal: " + e.getMessage());
            e.printStackTrace();
        }
        return archivoTemp;
    }

    public File base64ToFileWithoutSplit(String base64String, String fileName) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);
            File file = new File(fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(decodedBytes);
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
