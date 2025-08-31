package com.marzook.pdfbackend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
public class PdfFileService {

    public record response(String pdf_key){}
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName = "cloudpdfs";


    PdfFileService(S3Client pdfs3Client, S3Presigner pdfs3Presigner) {
        this.s3Presigner = pdfs3Presigner;
        this.s3Client = pdfs3Client;
    }


    public response UploadPdfFile(MultipartFile file) throws IOException {
        String uniqueFileName = UUID.randomUUID() + "-" + file.getOriginalFilename();


        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(uniqueFileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));


        return new response(uniqueFileName);
    }


    public String generatePdfDownloadURL(String key, int expirationInMinutes) {
        // 1. Create a GetObjectRequest
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        // 2. Create a GetObjectPresignRequest
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationInMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        // 3. Generate the presigned request
        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(presignRequest);

        // 4. Get the URL and return it as a string
        return presignedGetObjectRequest.url().toString();
    }
    public String generatePdfViewableURL(String key, int expirationInMinutes) {
        // 1. Create a GetObjectRequest
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .responseContentDisposition("inline")
                .key(key)
                .build();

        // 2. Create a GetObjectPresignRequest
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationInMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        // 3. Generate the presigned request
        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(presignRequest);

        // 4. Get the URL and return it as a string
        return presignedGetObjectRequest.url().toString();
    }
}
