package org.example.crudservice.dtos.Response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LookupResponse {
    private Long id;
    private String isoCode;
    private String name;
    private String capitalCity;
    private String phoneCode;
    private String continentCode;
    private String currencyCode;
    private String flagUrl;
    private java.util.List<LanguageDto> languages;
}