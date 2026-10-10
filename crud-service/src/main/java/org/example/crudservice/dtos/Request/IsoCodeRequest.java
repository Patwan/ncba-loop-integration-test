package org.example.crudservice.dtos.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.example.crudservice.validation.SentenceCase;

@Data
public class IsoCodeRequest {

    @NotBlank(message = "Country name is required")
    @SentenceCase
    private String countryName;
}