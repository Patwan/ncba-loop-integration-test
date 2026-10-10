package org.example.crudservice.dtos.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> implements Serializable {

    private String status;
    private String message;
    private T data;
    private Instant requestTime;
    private Instant responseTime;
    private Long durationMs;

    public static <T> ApiResponse<T> success(T data, String message, Instant requestTime) {
        Instant now = Instant.now();
        Instant start = requestTime != null ? requestTime : now;   // ← defensive
        return ApiResponse.<T>builder()
                .status("SUCCESS")
                .message(message)
                .data(data)
                .requestTime(start)
                .responseTime(now)
                .durationMs(now.toEpochMilli() - start.toEpochMilli())
                .build();
    }

    public static <T> ApiResponse<T> successNoData(String message, Instant requestTime) {
        return success(null, message, requestTime);
    }

    public static <T> ApiResponse<T> failure(String message, Instant requestTime) {
        Instant now = Instant.now();
        return ApiResponse.<T>builder()
                .status("ERROR")
                .message(message)
                .requestTime(requestTime)
                .responseTime(now)
                .durationMs(now.toEpochMilli() - requestTime.toEpochMilli())
                .build();
    }
}