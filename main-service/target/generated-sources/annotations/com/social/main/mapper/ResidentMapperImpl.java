package com.social.main.mapper;

import com.social.main.dto.request.ResidentRequest;
import com.social.main.dto.response.ResidentResponse;
import com.social.main.entity.Resident;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-04T11:37:23+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Ubuntu)"
)
@Component
public class ResidentMapperImpl implements ResidentMapper {

    @Override
    public Resident toEntity(ResidentRequest request) {
        if ( request == null ) {
            return null;
        }

        Resident.ResidentBuilder resident = Resident.builder();

        resident.numeroCni( request.getNumeroCni() );
        resident.nom( request.getNom() );
        resident.prenom( request.getPrenom() );
        resident.nationalite( request.getNationalite() );
        resident.niveauDiplome( request.getNiveauDiplome() );
        resident.trancheSalariale( request.getTrancheSalariale() );
        resident.dateNaissance( request.getDateNaissance() );
        resident.telephone( request.getTelephone() );
        resident.chef( request.isChef() );

        return resident.build();
    }

    @Override
    public ResidentResponse toResponse(Resident resident) {
        if ( resident == null ) {
            return null;
        }

        ResidentResponse residentResponse = new ResidentResponse();

        residentResponse.setId( resident.getId() );
        residentResponse.setNumeroCni( resident.getNumeroCni() );
        residentResponse.setNom( resident.getNom() );
        residentResponse.setPrenom( resident.getPrenom() );
        residentResponse.setNationalite( resident.getNationalite() );
        residentResponse.setNiveauDiplome( resident.getNiveauDiplome() );
        residentResponse.setTrancheSalariale( resident.getTrancheSalariale() );
        residentResponse.setDateNaissance( resident.getDateNaissance() );
        residentResponse.setTelephone( resident.getTelephone() );
        residentResponse.setChef( resident.isChef() );

        residentResponse.setAge( calculateAge(resident.getDateNaissance()) );

        return residentResponse;
    }
}
