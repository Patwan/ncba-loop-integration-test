package org.example.crudservice.controllers;

import lombok.extern.slf4j.Slf4j;
import org.example.crudservice.dtos.Response.CountryInfoResponse;
import org.example.crudservice.dtos.Response.LookupResponse;
import org.example.crudservice.dtos.Response.ApiResponse;
import org.example.crudservice.dtos.Request.CountryRequest;
import org.example.crudservice.dtos.Request.IsoCodeRequest;
import org.example.crudservice.dtos.Request.CountryInfoRequest;
import org.example.crudservice.services.IsoService;
import org.springframework.http.HttpStatus;
import java.time.Instant;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/countries")
@RequiredArgsConstructor
public class IsoController {

    private final IsoService isoService;

    @PostMapping("/sentence-case")
    public ResponseEntity<ApiResponse> convertToSentenceCase(
            @Valid @RequestBody CountryRequest request) {

        String normalized = isoService.convertToSentenceCase(request.getName());
        Instant requestTime = Instant.now();

        ApiResponse<String> body = ApiResponse.success(
                normalized,
                "Input converted  to sentence case successfully",
                requestTime
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/fetch-iso-code")
    public ResponseEntity<ApiResponse> fetchIsoCode(
            @Valid @RequestBody IsoCodeRequest request) {

        String response = isoService.fetchIsoCodeByCountryName(request.getCountryName());
        Instant requestTime = Instant.now();

        ApiResponse<String> body = ApiResponse.success(
                response,
                "Country ISO Code Ferched Successfully",
                requestTime
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }


    @PostMapping("/fetch-full-country-info")
    public ResponseEntity<ApiResponse> fetchFullCountryInfo(
            @Valid @RequestBody CountryInfoRequest request) {

        CountryInfoResponse response = isoService.fetchFullCountryInfoByIsoCode(request.getIsoCode());
        Instant requestTime = Instant.now();

        ApiResponse<CountryInfoResponse> body = ApiResponse.success(
                response,
                "Full Country Info fetched and Persisted Successfully",
                requestTime
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }


}