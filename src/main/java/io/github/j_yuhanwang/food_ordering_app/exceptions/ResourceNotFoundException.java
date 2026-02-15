package io.github.j_yuhanwang.food_ordering_app.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author YuhanWang
 * @Date 04/02/2026 9:53 pm
 */
@Getter
@ResponseStatus(value= HttpStatus.NOT_FOUND) //404
public class ResourceNotFoundException extends RuntimeException{

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName,Object fieldValue){

//        Usage：throw new ResourceNotFoundException("User", "id", 100L);
//        Outcome：User not found with id : '100'
        super(String.format("%s not found with %s: : '%s'",resourceName,fieldName,fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

}
