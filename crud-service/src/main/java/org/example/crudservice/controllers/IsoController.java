package org.example.crudservice.controllers;

import org.example.crudservice.dtos.Response.LookupResponse;
import org.example.crudservice.dtos.Request.CountryRequest;
import org.example.crudservice.services.IsoService;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/countries")
@RequiredArgsConstructor
public class IsoController {

    private final IsoService countryService;


    @PostMapping("/save")
    public ResponseEntity<LookupResponse> lookup(
            @Valid @RequestBody CountryRequest request) {

        String normalized = countryService.convertToSentenceCase(request.getName());

        LookupResponse response = countryService.fetchIsoCodeByCountryName(normalized);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


}