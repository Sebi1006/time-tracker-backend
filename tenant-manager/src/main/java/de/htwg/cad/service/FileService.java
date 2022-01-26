package de.htwg.cad.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import de.htwg.cad.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@Service
public class FileService {
    @Autowired
    private AmazonS3 amazonS3;

    private File convertMultiPartFileToFile(final MultipartFile multipartFile) {
        final File file = new File(multipartFile.getOriginalFilename());

        try (final FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(multipartFile.getBytes());
        } catch (IOException e) {
            log.error("Error {} occurred while converting the multipart file.", e.getLocalizedMessage());
        }

        return file;
    }

    @Async
    public S3ObjectInputStream findByName(String fileName) {
        log.info("Downloading file with name: {}", fileName);
        return amazonS3.getObject(TenantContext.getTenantId() + "-s3-bucket", fileName).getObjectContent();
    }

    @Async
    public void save(MultipartFile multipartFile, String fileName) {
        try {
            final File file = convertMultiPartFileToFile(multipartFile);
            String extension = "";
            int i = file.getName().lastIndexOf('.');
            if (i > 0) {
                extension = "." + file.getName().substring(i + 1);
            }
            log.info("Uploading file with name: {}", fileName + extension);
            final PutObjectRequest putObjectRequest = new PutObjectRequest(TenantContext.getTenantId() + "-s3-bucket", fileName + extension, file);
            amazonS3.putObject(putObjectRequest);
            Files.delete(file.toPath());
        } catch (AmazonServiceException e) {
            log.error("Error {} occurred while uploading file.", e.getLocalizedMessage());
        } catch (IOException e) {
            log.error("Error {} occurred while deleting temporary file.", e.getLocalizedMessage());
        }
    }
}
