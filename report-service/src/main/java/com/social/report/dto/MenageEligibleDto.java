package com.social.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenageEligibleDto {
    private UUID menageId;
    private String codeMenage;
    private String chefNom;
    private String chefContact;
    private int score;
    private String categorie;
    private String region;
    private String ville;
    private int nombreResidents;
    private boolean beneficiaireActuel;

    // Constructeur manuel si Lombok ne fonctionne pas
    public static class MenageEligibleDtoBuilder {
        private UUID menageId;
        private String codeMenage;
        private String chefNom;
        private String chefContact;
        private int score;
        private String categorie;
        private String region;
        private String ville;
        private int nombreResidents;
        private boolean beneficiaireActuel;

        public MenageEligibleDtoBuilder menageId(UUID menageId) {
            this.menageId = menageId;
            return this;
        }

        public MenageEligibleDtoBuilder codeMenage(String codeMenage) {
            this.codeMenage = codeMenage;
            return this;
        }

        public MenageEligibleDtoBuilder chefNom(String chefNom) {
            this.chefNom = chefNom;
            return this;
        }

        public MenageEligibleDtoBuilder chefContact(String chefContact) {
            this.chefContact = chefContact;
            return this;
        }

        public MenageEligibleDtoBuilder score(int score) {
            this.score = score;
            return this;
        }

        public MenageEligibleDtoBuilder categorie(String categorie) {
            this.categorie = categorie;
            return this;
        }

        public MenageEligibleDtoBuilder region(String region) {
            this.region = region;
            return this;
        }

        public MenageEligibleDtoBuilder ville(String ville) {
            this.ville = ville;
            return this;
        }

        public MenageEligibleDtoBuilder nombreResidents(int nombreResidents) {
            this.nombreResidents = nombreResidents;
            return this;
        }

        public MenageEligibleDtoBuilder beneficiaireActuel(boolean beneficiaireActuel) {
            this.beneficiaireActuel = beneficiaireActuel;
            return this;
        }

        public MenageEligibleDto build() {
            MenageEligibleDto dto = new MenageEligibleDto();
            dto.menageId = this.menageId;
            dto.codeMenage = this.codeMenage;
            dto.chefNom = this.chefNom;
            dto.chefContact = this.chefContact;
            dto.score = this.score;
            dto.categorie = this.categorie;
            dto.region = this.region;
            dto.ville = this.ville;
            dto.nombreResidents = this.nombreResidents;
            dto.beneficiaireActuel = this.beneficiaireActuel;
            return dto;
        }
    }

    public static MenageEligibleDtoBuilder builder() {
        return new MenageEligibleDtoBuilder();
    }
}