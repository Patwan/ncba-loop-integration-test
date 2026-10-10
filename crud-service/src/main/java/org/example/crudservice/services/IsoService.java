package org.example.crudservice.services;

import org.example.crudservice.dtos.Response.LookupResponse;

public interface IsoService {

    LookupResponse fetchIsoCodeByCountryName(String countryName);

    String convertToSentenceCase(String input);
}