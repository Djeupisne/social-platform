// auth-service/src/main/java/com/social/auth/dto/response/ValidationErrorResponse.java
package com.social.auth.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ValidationErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private Map<String, String> fieldErrors;
    private String path;
}