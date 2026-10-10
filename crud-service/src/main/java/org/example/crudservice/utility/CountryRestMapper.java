package org.example.crudservice.utility;


import org.example.crudservice.dtos.Request.CountryUpdateRequest;
import org.example.crudservice.dtos.Response.CountryResponse;
import org.example.crudservice.dtos.Response.LanguageDto;
import org.example.crudservice.entities.CountryInfo;
import org.example.crudservice.entities.Language;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CountryRestMapper {

    CountryResponse toResponse(CountryInfo entity);

    LanguageDto toLanguageDto(Language entity);

    List<CountryResponse> toResponseList(List<CountryInfo> entities);

    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "isoCode",         ignore = true)   // ISO is immutable
    @Mapping(target = "languages",       ignore = true)   // handled in service
    void updateEntityFromRequest(CountryUpdateRequest request,
                                 @MappingTarget CountryInfo entity);

    @Mapping(target = "id",      ignore = true)
    @Mapping(target = "country", ignore = true)
    Language toLanguageEntity(LanguageDto dto);
}