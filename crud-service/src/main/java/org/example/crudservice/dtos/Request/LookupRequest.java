package org.example.crudservice.dtos.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LookupRequest {

    @NotBlank(message = "Country name is required")
    private String name;

    private String capitalCity;
    private String phoneCode;
    private String currencyName;
}