package org.example.crudservice.dtos.Request;

import org.example.crudservice.dtos.Response.LanguageDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountryUpdateRequest {

    @NotBlank(message = "Country name must not be blank")
    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String capitalCity;

    @Size(max = 10)
    private String phoneCode;

    @Size(max = 2)
    private String continentCode;

    @Size(max = 3)
    private String currencyIsoCode;

    @Size(max = 100)
    private String currencyName;

    @Size(max = 512)
    private String flagUrl;

    @Valid
    private List<LanguageDto> languages;
}