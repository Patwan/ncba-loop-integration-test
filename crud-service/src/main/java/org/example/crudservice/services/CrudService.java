package org.example.crudservice.services;

import org.example.crudservice.dtos.Request.CountryUpdateRequest;
import org.example.crudservice.dtos.Response.CountryResponse;
import org.example.crudservice.dtos.Response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CrudService {

    PageResponse<CountryResponse> findAll(Pageable pageable);

    CountryResponse findById(Long id);

    CountryResponse update(Long id, CountryUpdateRequest request);

    void delete(Long id);
}