package edu.jecrc.portal.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private boolean success;
    private String errorCode;
    private String message;
    private List<String> details;
    @Builder.Default
    private Instant timestamp = Instant.now();
    private String path;

    public static ApiError of(String errorCode, String message, String path) {
        return ApiError.builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .build();
    }

    public static ApiError of(String errorCode, String message, List<String> details, String path) {
        return ApiError.builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .details(details)
                .path(path)
                .build();
    }
}
