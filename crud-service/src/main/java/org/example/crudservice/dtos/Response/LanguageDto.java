package org.example.crudservice.dtos.Response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageDto {
    private Long id;
    private String code;
    private String name;
}