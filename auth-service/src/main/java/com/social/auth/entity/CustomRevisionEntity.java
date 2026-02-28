package com.social.auth.entity;

import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "revision_info")
@RevisionEntity(CustomRevisionListener.class)
@Getter
@Setter
@NoArgsConstructor
public class CustomRevisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    private Long id;

    @RevisionTimestamp
    private Date timestamp;

    @Column(name = "username")
    private String username;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "action")
    private String action;

    @Column(name = "ip_address")
    private String ipAddress;
}