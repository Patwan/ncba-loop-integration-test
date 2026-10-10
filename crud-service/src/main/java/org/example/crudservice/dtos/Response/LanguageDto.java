package org.example.crudservice.dtos.Response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageDto {

    @Size(max = 3)
    private String isoCode;

    @NotBlank
    @Size(max = 100)
    private String name;
}