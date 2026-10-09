package org.example.crudservice.controllers;

import org.example.crudservice.dtos.Request.LookupRequest;
import org.example.crudservice.dtos.Response.LookupResponse;
import org.example.crudservice.services.CountryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class LookUpController {

    private final CountryService countryService;

    /**
     * POST /api/countries/lookup
     * Body: { "name": "tanzania" }
     *
     * Fetches ISO code via SOAP, then full country info, and persists.
     */
    @PostMapping("/lookup")
    public ResponseEntity<LookupResponse> lookup(
            @Valid @RequestBody LookupRequest request) {

        LookupResponse response =
                countryService.lookupAndPersist(request.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}