package com.social.auth.dto;

import com.social.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuditDto {
    private User user;
    private Long revisionNumber;
    private Instant revisionDate;
    private String revisionType;  // ADD, MOD, DEL
    private String username;
    private String userEmail;
    private String action;
    private String ipAddress;
}