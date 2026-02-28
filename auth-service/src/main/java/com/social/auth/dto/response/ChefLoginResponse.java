package com.social.auth.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ChefLoginResponse {
    private String  token;
    private String  refreshToken;
    private UUID    chefId;
    private UUID    menageId;
    private String  nomComplet;
    private String  role;
    private boolean twoFactorRequired;
    private String  error;
}
