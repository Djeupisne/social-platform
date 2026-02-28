package com.social.main.dto.response;

import com.social.main.enums.NiveauDiplome;
import com.social.main.enums.TrancheSalariale;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ResidentResponse {
    private UUID id;
    private String numeroCni;
    private String nom;
    private String prenom;
    private String nationalite;
    private NiveauDiplome niveauDiplome;
    private TrancheSalariale trancheSalariale;
    private LocalDate dateNaissance;
    private String telephone;
    private boolean chef;
    private int age;
}