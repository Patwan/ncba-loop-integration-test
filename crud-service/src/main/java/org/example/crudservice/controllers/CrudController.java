package org.example.crudservice.controllers;

import org.example.crudservice.dtos.Response.LookupResponse;
import org.example.crudservice.dtos.Request.LookupRequest;
import org.example.crudservice.dtos.Request.CountryRequest;
import org.example.crudservice.services.PersistCountry;
import org.example.crudservice.services.CountryService;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/countries")
@RequiredArgsConstructor
public class CrudController {

    private final PersistCountry persistCountry;
    private final CountryService countryService;


    @PostMapping("/save")
    public ResponseEntity<LookupResponse> lookup(
            @Valid @RequestBody CountryRequest request) {

        // 1. Normalize the incoming name
        String normalized = countryService.convertToSentenceCase(request.getName());

        LookupResponse response = countryService.lookupAndPersist(normalized);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/countries?page=0&size=10&sort=name,asc
     *
     */
    @GetMapping
    public ResponseEntity<Page<LookupResponse>> findAll(
            @PageableDefault(
                    size = 10,
                    sort = "name",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {

        return ResponseEntity.ok(persistCountry.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LookupResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(persistCountry.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LookupResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody LookupRequest request) {
        return ResponseEntity.ok(persistCountry.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        persistCountry.delete(id);
        return ResponseEntity.noContent().build();
    }
}