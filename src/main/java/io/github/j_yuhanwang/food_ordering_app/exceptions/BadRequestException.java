package io.github.j_yuhanwang.food_ordering_app.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception to indicate that a requested resource was not found.
 * Inherits from RuntimeException to avoid mandatory try-catch blocks (Unchecked Exception).
 * @author YuhanWang
 * @Date 15/02/2026 2:44 pm
 */

@ResponseStatus(value= HttpStatus.BAD_REQUEST) //400
public class BadRequestException extends RuntimeException{

    /**
     * Pass the custom message to the parent RuntimeException constructor.
     * @param message Detailed error information.
     */
    public BadRequestException(String message){
        super(message);
    }
}