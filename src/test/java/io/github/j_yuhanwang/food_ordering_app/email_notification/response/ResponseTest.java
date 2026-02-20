package io.github.j_yuhanwang.food_ordering_app.email_notification.response;

import io.github.j_yuhanwang.food_ordering_app.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author YuhanWang
 * @Date 20/02/2026 12:20 pm
 */

public class ResponseTest {
    //1.success response: only have the data
    @Test
    @DisplayName("Testing1: ok(data) - only contains data, uses default 200 and Success message")
    void testOkWithDataOnly(){
        //Arrange
        String testData = "Pi restaurant";

        //Act
        Response<String> response = Response.ok(testData);

        //Assert
        assertEquals(200,response.getStatusCode(),"The default status code should be 200.");
        assertEquals("Success",response.getMessage(),"The default message should be 'Success'");
        assertEquals(testData,response.getData(),"Data payload does not match the input.");
        assertNull(response.getMeta(),"Meta should be null when not provided.");
        assertNotNull(response.getTimestamp(),"Timestamps should be generated automatically.");
    }

    //2.success response: convey the data and the message
    @Test
    @DisplayName("Testing2: ok(data, message) - contains data and customized message, uses default 200")
    void testOkWithDataAndCustomMessage(){
        //Arrange
        String testData = "Buzz Coffee";
        String customMsg = "Menu fetched successfully";

        //Act
        Response<String> response = Response.ok(testData,customMsg);

        //Assert
        assertEquals(200,response.getStatusCode(),"The default status code should be 200.");
        assertEquals(customMsg,response.getMessage(),"The message does not match the input.");
        assertEquals(testData,response.getData(),"Data payload does not match the input.");
        assertNull(response.getMeta(),"Meta data should be null when not provided.");
        assertNotNull(response.getTimestamp(),"Timestamps should be generated automatically.");
    }
    //3.success response: no arguments
    @Test
    @DisplayName("Testing3: ok() - no arguments, returns 200 and default success without data")
    void testOkWithNoArguments(){
        //Arrange
        //Act
        Response<Void> response = Response.ok();

        //Assert
        assertEquals(200,response.getStatusCode(),"The default status code should be 200.");
        assertEquals("Success",response.getMessage(),"The default message should be 'Success'.");
        assertNull(response.getData(),"Data should be null when not provided.");
        assertNull(response.getMeta(),"Meta data should be null when not provided.");
        assertNotNull(response.getTimestamp(),"Timestamps should be generated automatically.");
    }
    //4.error response: standard exception(statusCode+message)
    @Test
    @DisplayName("Testing4: error(statusCode,message) - should return standard error response with custom code and message")
    void testErrorWithStatusCodeAndMsg(){
        //Arrange
        int errorCode = 404;
        String errorMsg = "User not found";

        //Act
        Response<Object> response = Response.error(errorCode,errorMsg);

        //Assert
        assertEquals(errorCode,response.getStatusCode());
        assertEquals(errorMsg,response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    //5.error response: exception with the error fields(statusCode+message+metadata)
    @Test
    @DisplayName("Testing5: error(statusCode,message,meta) - should include contextual metadata for detailed validation errors")
    void testErrorWithStatusCodeMsgMeta(){
        //Arrange
        int errorCode = 400;
        String errorMsg = "Validation failed";
        Map<String,Object> errorDetails = new HashMap<>();
        errorDetails.put("email","Invalid format");
        errorDetails.put("password","Too weak");

        //Act
        Response<Object> response = Response.error(errorCode,errorMsg,errorDetails);

        //Assert
        assertEquals(errorCode,response.getStatusCode());
        assertEquals(errorMsg,response.getMessage());
        assertNotNull(response.getTimestamp());

        //Test the meta data:size, content, not null
        assertNotNull(response.getMeta(),"Meta map should not be null.");
        assertEquals(2,response.getMeta().size(),"Meta should contain exactly 2 error fields.");
        assertEquals("Invalid format",response.getMeta().get("email"));
    }
}
