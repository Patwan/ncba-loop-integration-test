package org.example.crudservice.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.crudservice.dtos.Request.CountryUpdateRequest;
import org.example.crudservice.dtos.Response.CountryResponse;
import org.example.crudservice.dtos.Response.PageResponse;
import org.example.crudservice.services.CrudService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api/v1/countries")
@RequiredArgsConstructor
public class CrudController {

    private final CrudService countryCrudService;

    @GetMapping
    public ResponseEntity<PageResponse<CountryResponse>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc")  String direction) {


        int safeSize = Math.min(size, 100);
        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, safeSize, sort);

        PageResponse<CountryResponse> body = countryCrudService.findAll(pageable);
        return ResponseEntity.ok(body);
    }


    @GetMapping("/{id}")
    public ResponseEntity<CountryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(countryCrudService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CountryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CountryUpdateRequest request) {

        CountryResponse updated = countryCrudService.update(id, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .buildAndExpand(id).toUri();
        return ResponseEntity.ok().location(location).body(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        countryCrudService.delete(id);
        return ResponseEntity.noContent().build();
    }
}