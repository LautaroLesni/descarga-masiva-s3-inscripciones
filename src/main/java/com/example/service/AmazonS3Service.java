package com.example.service;

import com.example.service.integration.AmazonClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import java.io.IOException;
import java.nio.file.Path;

@ApplicationScoped
public class AmazonS3Service {

    protected final Logger logger = Logger.getLogger(getClass());

    // Inject the managed bean directly
    @Inject
    AmazonClient amazonClient;

    public Path getFileResource(String nameResource) throws IOException {
        return amazonClient.getResource(nameResource);
    }
}