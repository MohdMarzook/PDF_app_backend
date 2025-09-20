package com.marzook.pdfbackend.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

@Service
public class HtmlFileService {

    public record response(String pdf_key){}
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName = "translatedpdf";


    HtmlFileService(S3Client htmls3Client, S3Presigner htmls3Presigner) {
        this.s3Client = htmls3Client;
        this.s3Presigner = htmls3Presigner;
    }


    public String generateHtmlViewableURL(String key, int expirationInMinutes) {
        // 1. Create a GetObjectRequest with "inline" disposition
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                // This is the key line for viewing the file in the browser
                .responseContentDisposition("inline")
                .build();

        // 2. Create the presign request
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationInMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        // 3. Generate the presigned URL
        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(presignRequest);

        return presignedGetObjectRequest.url().toString();
    }

    public String generateHtmlDownloadableURL(String key, int expirationInMinutes) {
        // 1. Create a GetObjectRequest with "attachment" disposition
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                // This is the key line for forcing a download
                .responseContentDisposition("attachment; filename=\"" + key + "\"")
                .build();

        // 2. Create the presign request
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationInMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        // 3. Generate the presigned URL
        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(presignRequest);

        return presignedGetObjectRequest.url().toString();
    }

    public void deleteHtml(String pdfkey){
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(pdfkey)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }
}
