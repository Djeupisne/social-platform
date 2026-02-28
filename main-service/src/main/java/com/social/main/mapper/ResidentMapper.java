package com.social.main.mapper;

import com.social.main.dto.request.ResidentRequest;
import com.social.main.dto.response.ResidentResponse;
import com.social.main.entity.Resident;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.Period;

@Mapper(componentModel = "spring")
public interface ResidentMapper {

    Resident toEntity(ResidentRequest request);

    @Mapping(target = "age", expression = "java(calculateAge(resident.getDateNaissance()))")
    ResidentResponse toResponse(Resident resident);

    default int calculateAge(LocalDate dateNaissance) {
        if (dateNaissance == null) return 0;
        return Period.between(dateNaissance, LocalDate.now()).getYears();
    }
}