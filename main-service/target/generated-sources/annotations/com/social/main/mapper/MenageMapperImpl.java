package com.social.main.mapper;

import com.social.main.dto.response.MenageDetailResponse;
import com.social.main.dto.response.MenageResponse;
import com.social.main.dto.response.ResidentResponse;
import com.social.main.entity.Menage;
import com.social.main.entity.Resident;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-01T21:49:48+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.17 (Eclipse Adoptium)"
)
@Component
public class MenageMapperImpl implements MenageMapper {

    @Autowired
    private ResidentMapper residentMapper;

    @Override
    public MenageResponse toResponse(Menage menage) {
        if ( menage == null ) {
            return null;
        }

        MenageResponse menageResponse = new MenageResponse();

        menageResponse.setId( menage.getId() );
        menageResponse.setCode( menage.getCode() );
        menageResponse.setATelevision( menage.isATelevision() );
        menageResponse.setARadio( menage.isARadio() );
        menageResponse.setAMoto( menage.isAMoto() );
        menageResponse.setAVoiture( menage.isAVoiture() );
        menageResponse.setStatutHabitation( menage.getStatutHabitation() );
        menageResponse.setScore( menage.getScore() );
        menageResponse.setCategorie( menage.getCategorie() );
        menageResponse.setRegion( menage.getRegion() );
        menageResponse.setVille( menage.getVille() );
        menageResponse.setQuartier( menage.getQuartier() );
        menageResponse.setAdresse( menage.getAdresse() );
        menageResponse.setCreatedAt( menage.getCreatedAt() );

        menageResponse.setNombreResidents( menage.getResidents().size() );
        menageResponse.setNomChef( menage.getChef() != null ? menage.getChef().getNom() + " " + menage.getChef().getPrenom() : "N/A" );
        menageResponse.setCategorieLabel( menage.getCategorie() != null ? menage.getCategorie().getLabel() : "" );

        return menageResponse;
    }

    @Override
    public MenageDetailResponse toDetailResponse(Menage menage) {
        if ( menage == null ) {
            return null;
        }

        MenageDetailResponse menageDetailResponse = new MenageDetailResponse();

        menageDetailResponse.setId( menage.getId() );
        menageDetailResponse.setCode( menage.getCode() );
        menageDetailResponse.setATelevision( menage.isATelevision() );
        menageDetailResponse.setARadio( menage.isARadio() );
        menageDetailResponse.setAMoto( menage.isAMoto() );
        menageDetailResponse.setAVoiture( menage.isAVoiture() );
        menageDetailResponse.setStatutHabitation( menage.getStatutHabitation() );
        menageDetailResponse.setScore( menage.getScore() );
        menageDetailResponse.setCategorie( menage.getCategorie() );
        menageDetailResponse.setRegion( menage.getRegion() );
        menageDetailResponse.setVille( menage.getVille() );
        menageDetailResponse.setQuartier( menage.getQuartier() );
        menageDetailResponse.setAdresse( menage.getAdresse() );
        menageDetailResponse.setResidents( residentListToResidentResponseList( menage.getResidents() ) );
        menageDetailResponse.setCreatedAt( menage.getCreatedAt() );
        menageDetailResponse.setUpdatedAt( menage.getUpdatedAt() );

        menageDetailResponse.setCategorieLabel( menage.getCategorie() != null ? menage.getCategorie().getLabel() : "" );

        return menageDetailResponse;
    }

    protected List<ResidentResponse> residentListToResidentResponseList(List<Resident> list) {
        if ( list == null ) {
            return null;
        }

        List<ResidentResponse> list1 = new ArrayList<ResidentResponse>( list.size() );
        for ( Resident resident : list ) {
            list1.add( residentMapper.toResponse( resident ) );
        }

        return list1;
    }
}
