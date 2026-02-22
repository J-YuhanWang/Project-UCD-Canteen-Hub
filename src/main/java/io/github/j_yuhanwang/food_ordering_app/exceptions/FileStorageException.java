package io.github.j_yuhanwang.food_ordering_app.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception to indicate a failure during file storage operations (e.g., AWS S3 network outages).
 * Inherits from RuntimeException to avoid mandatory try-catch blocks (Unchecked Exception).
 * @author YuhanWang
 * @Date 15/02/2026 2:44 pm
 */

@ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR) //500
public class FileStorageException extends RuntimeException{

    /**
     * Throwable cause can send the original error information from the AWS SDK to the super,
     * making it easy to check in the server logs whether the problem is a network outage or a permission error.
     */
    public FileStorageException(String message,Throwable cause){
        super(message,cause);
    }
}