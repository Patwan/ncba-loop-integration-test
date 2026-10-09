package org.example.crudservice.utility;

import org.example.crudservice.dtos.Response.LookupResponse;
import org.example.crudservice.dtos.Response.LanguageDto;
import org.example.crudservice.Entities.Language;
import org.example.crudservice.Entities.CountryInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CountryMapper {

    LookupResponse toDto(CountryInfo entity);

    LanguageDto toDto(Language entity);

    List<LookupResponse> toDtoList(List<CountryInfo> entities);
}