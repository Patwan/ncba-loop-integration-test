package org.example.crudservice.services;

import org.example.crudservice.dtos.Response.LookupResponse;

public interface CountryService {

    LookupResponse lookupAndPersist(String countryName);

    String convertToSentenceCase(String input);
}