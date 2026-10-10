package org.example.crudservice.dtos.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CountryInfoRequest {

    @NotBlank(message = "Please Pass the Country ISO Code")
    private String isoCode;
}
