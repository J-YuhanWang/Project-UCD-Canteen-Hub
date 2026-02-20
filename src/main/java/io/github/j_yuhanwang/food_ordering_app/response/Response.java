package io.github.j_yuhanwang.food_ordering_app.response;/*
 * @author BlairWang
 * @Date 16/12/2025 6:33 pm
 * @Version 1.0
 */

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {

    private int statusCode; // e.g.,"200", "404"
    private String message; // Additional information about the response
    private T data; // The actual data payload
    private Map<String, Object> meta;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    //1.success response: only have the data
    public static <T> Response<T> ok(T data){
        return Response.<T>builder()
                .statusCode(200)
                .message("Success")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    //2.success response: convey the data and the message
    public static <T> Response<T> ok(T data, String message){
        return Response.<T>builder()
                .statusCode(200)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    //3.success response: no arguments
    public static <T> Response<T> ok(){
        return Response.<T>builder()
                .statusCode(200)
                .message("Success")
                .timestamp(LocalDateTime.now())
                .build();
    }

    //4.error response: standard exception(statusCode+message)
    public static <T> Response<T> error(int statusCode, String message){
        return Response.<T>builder()
                .statusCode(statusCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    //5.error response: exception with the error fields(statusCode+message+metadata)
    public static <T> Response<T> error(int statusCode, String message, Map<String,Object> meta){
        return Response.<T>builder()
                .statusCode(statusCode)
                .message(message)
                .meta(meta)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
