package org.example.crudservice.utility;

import com.developerhut.intothespring.ws.TCountryInfo;
import com.developerhut.intothespring.ws.TLanguage;
import org.example.crudservice.dtos.Response.CountryInfoResponse;
import org.example.crudservice.dtos.Response.LanguageDto;
import org.example.crudservice.dtos.Response.LookupResponse;
import org.example.crudservice.entities.CountryInfo;
import org.example.crudservice.entities.Language;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CountryMapper {


    LookupResponse toLookupResponse(CountryInfo entity);

    LanguageDto toDto(Language entity);

    List<LookupResponse> toDtoList(List<CountryInfo> entities);

    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "isoCode",         source = "SISOCode")
    @Mapping(target = "name",            source = "SName")
    @Mapping(target = "capitalCity",     source = "SCapitalCity")
    @Mapping(target = "phoneCode",       source = "SPhoneCode")
    @Mapping(target = "continentCode",   source = "SContinentCode")
    @Mapping(target = "currencyIsoCode", source = "SCurrencyISOCode")
    @Mapping(target = "currencyName",    ignore = true)   // not in WSDL; enriched in service
    @Mapping(target = "flagUrl",         source = "SCountryFlag")
    @Mapping(target = "languages",
            expression = "java(mapLanguages(soapCountry.getLanguages() == null "
                    + "? null : soapCountry.getLanguages().getTLanguage()))")
    CountryInfo toEntity(TCountryInfo soapCountry);


    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "isoCode",         ignore = true)
    @Mapping(target = "name",            source = "SName")
    @Mapping(target = "capitalCity",     source = "SCapitalCity")
    @Mapping(target = "phoneCode",       source = "SPhoneCode")
    @Mapping(target = "continentCode",   source = "SContinentCode")
    @Mapping(target = "currencyIsoCode", source = "SCurrencyISOCode")
    @Mapping(target = "currencyName",    ignore = true)
    @Mapping(target = "flagUrl",         source = "SCountryFlag")
    @Mapping(target = "languages",       ignore = true)
    void updateEntityFromSoap(TCountryInfo soapCountry, @MappingTarget CountryInfo entity);

    @Named("mapLanguages")
    default List<Language> mapLanguages(List<TLanguage> soapLanguages) {
        if (soapLanguages == null) {
            return new ArrayList<>();
        }
        List<Language> result = new ArrayList<>(soapLanguages.size());
        for (TLanguage t : soapLanguages) {
            if (t == null) continue;
            Language lang = new Language();
            lang.setIsoCode(t.getSISOCode());
            lang.setName(t.getSName());
            result.add(lang);
        }
        return result;
    }


    @Mapping(target = "isoCode",         source = "SISOCode")
    @Mapping(target = "name",            source = "SName")
    @Mapping(target = "capitalCity",     source = "SCapitalCity")
    @Mapping(target = "phoneCode",       source = "SPhoneCode")
    @Mapping(target = "continentCode",   source = "SContinentCode")
    @Mapping(target = "currencyIsoCode", source = "SCurrencyISOCode")
    @Mapping(target = "currencyName",    ignore = true)    // enriched later
    @Mapping(target = "flagUrl",         source = "SCountryFlag")
    @Mapping(target = "languages",       source = "languages.TLanguage",
            qualifiedByName = "mapLanguagesToDto")
    CountryInfoResponse toResponse(TCountryInfo soapCountry);

    @Named("mapLanguagesToDto")
    default List<CountryInfoResponse.LanguageDto> mapLanguagesToDto(List<TLanguage> soapLanguages) {
        if (soapLanguages == null) return List.of();
        return soapLanguages.stream()
                .filter(java.util.Objects::nonNull)
                .map(t -> CountryInfoResponse.LanguageDto.builder()
                        .isoCode(t.getSISOCode())
                        .name(t.getSName())
                        .build())
                .toList();
    }

    default Language toEntity(TLanguage soapLanguage) {
        if (soapLanguage == null) return null;
        Language lang = new Language();
        lang.setIsoCode(soapLanguage.getSISOCode());
        lang.setName(soapLanguage.getSName());
        return lang;
    }
}