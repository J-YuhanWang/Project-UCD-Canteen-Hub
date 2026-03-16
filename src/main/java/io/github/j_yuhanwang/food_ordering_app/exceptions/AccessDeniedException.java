package io.github.j_yuhanwang.food_ordering_app.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Authorization Failed / Access Refused
 * @author YuhanWang
 * @Date 15/02/2026 2:44 pm
 */

@ResponseStatus(value= HttpStatus.FORBIDDEN) //403: Insufficient authorization
public class AccessDeniedException extends RuntimeException{

    /**
     * Pass the custom message to the parent RuntimeException constructor.
     * @param message Detailed error information.
     */
    public AccessDeniedException(String message){
        super(message);
    }
}