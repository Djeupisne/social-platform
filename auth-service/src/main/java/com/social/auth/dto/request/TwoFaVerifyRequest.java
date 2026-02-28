package com.social.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TwoFaVerifyRequest {
    @NotBlank
    private String email;
    @NotBlank
    private String code;
    private String tempToken; // token temporaire avant 2FA
}