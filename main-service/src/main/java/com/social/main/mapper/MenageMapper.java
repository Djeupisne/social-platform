package com.social.main.mapper;

import com.social.main.dto.response.MenageDetailResponse;
import com.social.main.dto.response.MenageResponse;
import com.social.main.entity.Menage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ResidentMapper.class})
public interface MenageMapper {

    @Mapping(target = "nombreResidents", expression = "java(menage.getResidents().size())")
    @Mapping(target = "nomChef", expression = "java(menage.getChef() != null ? menage.getChef().getNom() + \" \" + menage.getChef().getPrenom() : \"N/A\")")
    @Mapping(target = "categorieLabel", expression = "java(menage.getCategorie() != null ? menage.getCategorie().getLabel() : \"\")")
    MenageResponse toResponse(Menage menage);

    @Mapping(target = "categorieLabel", expression = "java(menage.getCategorie() != null ? menage.getCategorie().getLabel() : \"\")")
    MenageDetailResponse toDetailResponse(Menage menage);
}
