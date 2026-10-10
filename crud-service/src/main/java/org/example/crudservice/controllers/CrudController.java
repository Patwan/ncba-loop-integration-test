package org.example.crudservice.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.crudservice.components.RequestContext;
import org.example.crudservice.dtos.Request.CountryUpdateRequest;
import org.example.crudservice.dtos.Response.ApiResponse;
import org.example.crudservice.dtos.Response.CountryResponse;
import org.example.crudservice.dtos.Response.PageResponse;
import org.example.crudservice.services.CrudService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/countries")
@RequiredArgsConstructor
public class CrudController {

    private final CrudService countryCrudService;
    private final RequestContext requestContext;

    @GetMapping("/fetch-all")
    public ResponseEntity<ApiResponse<PageResponse<CountryResponse>>> getAll(
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "20")   int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc")  String direction) {

        int safeSize = Math.min(size, 100);
        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, safeSize, sort);

        PageResponse<CountryResponse> data = countryCrudService.findAll(pageable);

        ApiResponse<PageResponse<CountryResponse>> body = ApiResponse.success(
                data,
                "Countries fetched successfully",
                requestContext.getRequestTime()
        );

        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CountryResponse>> getById(@PathVariable Long id) {
        CountryResponse data = countryCrudService.findById(id);
        ApiResponse<CountryResponse> body = ApiResponse.success(
                data,
                "Country fetched successfully",
                requestContext.getRequestTime()
        );
        return ResponseEntity.ok(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CountryResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CountryUpdateRequest request) {

        CountryResponse data = countryCrudService.update(id, request);
        ApiResponse<CountryResponse> body = ApiResponse.success(
                data,
                "Country updated successfully",
                requestContext.getRequestTime()
        );
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        countryCrudService.delete(id);
        ApiResponse<Void> body = ApiResponse.successNoData(
                "Country deleted successfully",
                requestContext.getRequestTime()
        );
        return ResponseEntity.ok(body);
    }
}