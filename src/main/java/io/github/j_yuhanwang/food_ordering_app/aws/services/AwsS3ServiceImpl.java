package io.github.j_yuhanwang.food_ordering_app.aws.services;

import io.github.j_yuhanwang.food_ordering_app.exceptions.AccessDeniedException;
import io.github.j_yuhanwang.food_ordering_app.exceptions.BadRequestException;
import io.github.j_yuhanwang.food_ordering_app.exceptions.FileStorageException;
import io.github.j_yuhanwang.food_ordering_app.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URL;

/**
 * AWS S3 Infrastructure Service Implementation.
 * * This service acts as an adapter to interact with Amazon S3.
 * It encapsulates the AWS SDK logic and translates all infrastructure-level
 * exceptions into domain-specific exceptions (e.g., FileStorageException)
 * to maintain clean architecture boundaries.
 *
 * @author YuhanWang
 * @Date 21/02/2026
 */
@Service
@Slf4j //print log
@RequiredArgsConstructor
public class AwsS3ServiceImpl implements AwsS3Service{

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Override
    public String uploadFile(String keyName, MultipartFile file) {
        log.info("Starting file upload to S3. KeyName: {}", keyName);
//        Fail-fast validation to prevent unnecessary external network calls.
        if (file.isEmpty()) {
            throw new BadRequestException("Cannot upload an empty file.");//400
        }
        try{
            //Construct the S3 payload.
            //Note: Content-Type must be explicitly set so the browser can render it directly
            //instead of forcing a download.
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .contentType(file.getContentType())
                    .build();
            // Execute the upload operation
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            URL url = s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(keyName));
            log.info("File uploaded successfully. URL:{}",url);

            return url.toString();

        } catch (IOException | S3Exception e) {
            /*
             * Exception Translation: Wrap underlying infrastructure/SDK exceptions
             * into our domain-specific FileStorageException.
             * This decouples the global exception handler from AWS-specific errors.
             */
            throw new FileStorageException("S3 Storage service is currently unavailable or file is corrupted.",e);//500
        }
    }

    @Override
    public void deleteFile(String keyName) {
        log.info("Deleting file from S3. KeyName: {}",keyName);
        if(keyName==null || keyName.trim().isEmpty()){
            throw new BadRequestException("KeyName to delete cannot be empty.");//400
        }
        try{
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File [{}] deleted successfully from S3.",keyName);

        }catch(S3Exception e){
            // Intercept specific AWS HTTP status codes for precise error mapping
            if(e.statusCode()==403){
                throw new AccessDeniedException("AWS IAM permission denied for deleting files.");
            }
            throw new FileStorageException("Failed to delete file from S3",e);
        }
    }
}
