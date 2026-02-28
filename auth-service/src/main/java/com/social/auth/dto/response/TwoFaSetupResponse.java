package com.social.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TwoFaSetupResponse {
    private String qrCodeUrl;
    private String qrCode;
    private String secret;
    private String message;
}