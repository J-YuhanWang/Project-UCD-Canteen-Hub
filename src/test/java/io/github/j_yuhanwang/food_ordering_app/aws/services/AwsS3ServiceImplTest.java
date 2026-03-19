package io.github.j_yuhanwang.food_ordering_app.aws.services;

import io.github.j_yuhanwang.food_ordering_app.exceptions.BadRequestException;
import io.github.j_yuhanwang.food_ordering_app.exceptions.FileStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


/**
 * Unit tests for AwsS3ServiceImpl.
 * * This test suite focuses on validating the infrastructure adapter logic,
 * ensuring proper error handling for AWS S3 interactions, and verifying that
 * domain exceptions are correctly thrown based on different S3 response scenarios.
 * @author YuhanWang
 * @Date 22/02/2026
 */

@ExtendWith(MockitoExtension.class)
public class AwsS3ServiceImplTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private MultipartFile mockFile;

    @InjectMocks
    private AwsS3ServiceImpl awsS3Service;

    /**
     * Initializes the test environment before each test case.
     * Uses reflection to inject private configuration properties.
     */
    @BeforeEach
    void setUp(){
        ReflectionTestUtils.setField(awsS3Service,"bucketName","ucd-canteen-app-dev");
    }

    // ==========================================
    // Upload File Tests
    // ==========================================

    /**
     * Testing the Happy Path:
     * Verifies that a valid file is successfully uploaded to S3 and returns the expected public URL string.
     */
    @Test
    @DisplayName("Upload File - Should successfully upload and return URL")
    //Roy Osherove nomenclature: MethodName_StateUnderTest_ExpectedBehavior
    void uploadFile_Success_ShouldReturnUrl() throws IOException {
        //1.arrange
        String keyName = "test-image.png";
        String expectedUrlString = "https://ucd-canteen-app-dev.s3.eu-west-1.amazonaws.com/test-image.png";
        URL expectedUrl = new URL(expectedUrlString);

        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getBytes()).thenReturn("dummy image data:".getBytes());
        when(mockFile.getContentType()).thenReturn("image/png");

        //URL url = s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(keyName));
        S3Utilities mockS3Utilities = mock(S3Utilities.class);
        when(s3Client.utilities()).thenReturn(mockS3Utilities);
        when(mockS3Utilities.getUrl(any(Consumer.class))).thenReturn(expectedUrl);

        //2.act
        String actualUrl = awsS3Service.uploadFile(keyName,mockFile);

        //3.assert
        assertEquals(expectedUrlString,actualUrl);
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    /**
     * Testing the Fail-Fast Logic:
     * Ensures that uploading an empty file triggers a BadRequestException
     * and prevents any unnecessary interactions with the S3 infrastructure.
     */
    @Test
    @DisplayName("Upload File - Should throw BadRequestException when file is empty")
    void uploadFile_WhenFileIsEmpty_ShouldThrowBadRequestException(){
        //1.Arrange
        when(mockFile.isEmpty()).thenReturn(true);

        //2.Act
        BadRequestException exception = assertThrows(BadRequestException.class,()->
            awsS3Service.uploadFile("test.png",mockFile)
        );

        //3.Assert
        assertEquals("Cannot upload an empty file.",exception.getMessage());

        verifyNoInteractions(s3Client);

    }

    /**
     * Testing Exception Translation:
     * Validates that infrastructure-level S3 exceptions are correctly intercepted
     * and translated into domain-specific FileStorageExceptions.
     */
    @Test
    @DisplayName("Upload File - Should throw FileStorageException when aws fails")
    void uploadFile_WhenAwsFails_ShouldThrowFileStorageException() throws IOException {
        //1.arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getBytes()).thenReturn("data".getBytes());
        when(mockFile.getContentType()).thenReturn("image/png");
        when(s3Client.putObject(any(PutObjectRequest.class),any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("Aws down").statusCode(500).build());
        //2.act
        //3.assert
        FileStorageException exception = assertThrows(FileStorageException.class,()->
                awsS3Service.uploadFile("fail.png",mockFile));
        assertTrue(exception.getMessage().contains("S3 Storage service is currently unavailable or file is corrupted."));
    }

    // ==========================================
    // Delete File Tests
    // ==========================================

    /**
     * Testing the Successful Deletion Path:
     * Verifies that the delete operation is correctly triggered
     * when a valid key name is provided.
     */
    @Test
    @DisplayName("Delete File - Should successfully call deleteObject")
    void deleteFile_Success_ShouldInvokeAwsSdk(){
        //act
        awsS3Service.deleteFile("test-file.png");
        //assert
        verify(s3Client,times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    /**
     * Testing Fail-Fast Validation for Deletion:
     * Verifies that providing a blank or null keyName triggers a BadRequestException,
     * preventing any invalid network calls to AWS S3.
     */
    @Test
    @DisplayName("Delete File: Should throw BadRequestException when keyName is empty")
    void deleteFile_WhenKeyNameIsEmpty_ShouldThrowBadRequestException(){
        //1.arrange
        String keyName = "   ";

        //2.act
        BadRequestException exception = assertThrows(BadRequestException.class,()->
                awsS3Service.deleteFile(keyName));

        //3.assert
        assertEquals("KeyName to delete cannot be empty.", exception.getMessage());
    }

    /**
     * Testing Security Boundary Handling:
     * Simulates an AWS 403 Forbidden response to ensure the service correctly
     * translates IAM permission issues into a domain-specific AccessDeniedException. [cite: 2026-02-02]
     */
    @Test
    @DisplayName("Delete File: Should throw AccessDeniedException when AWS returns 403")
    void deleteFile_WhenAwsReturns403_ShouldThrowAccessDeniedException(){
        //1.arrange
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("Forbidden").statusCode(403).build());
        //2.act
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,()->
                awsS3Service.deleteFile("forbidden.png"));

        //3.assert
        assertEquals("AWS IAM permission denied for deleting files.",exception.getMessage());
    }
}
