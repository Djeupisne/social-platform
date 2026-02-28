package com.social.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;  // IMPORT MANQUANT AJOUTÉ

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;                    // Maintenant UUID est reconnu
    private String email;
    private String fullName;
    private String role;
    private String numeroCni;
    private boolean twoFactorEnabled;
    private boolean twoFactorVerified;
    private LocalDateTime createdAt;
    private UUID menageId;               // UUID est reconnu
    private String error;
}