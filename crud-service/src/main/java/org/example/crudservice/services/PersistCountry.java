package org.example.crudservice.services;

import org.example.crudservice.dtos.Response.LookupResponse;
import org.example.crudservice.dtos.Request.LookupRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PersistCountry {

    Page<LookupResponse> findAll(Pageable pageable);

    LookupResponse findById(Long id);

    LookupResponse update(Long id, LookupRequest request);

    void delete(Long id);
}