package org.example.crudservice.dtos.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CountryInfoResponse implements Serializable {

    private String isoCode;
    private String name;
    private String capitalCity;
    private String phoneCode;
    private String continentCode;
    private String currencyIsoCode;
    private String currencyName;
    private String flagUrl;
    private List<LanguageDto> languages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LanguageDto implements Serializable {
        private String isoCode;
        private String name;
    }
}