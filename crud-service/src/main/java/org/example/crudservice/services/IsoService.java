package org.example.crudservice.services;

import org.example.crudservice.dtos.Response.CountryInfoResponse;

public interface IsoService {

    String fetchIsoCodeByCountryName(String countryName);

    String convertToSentenceCase(String input);

    CountryInfoResponse fetchFullCountryInfoByIsoCode(String input);
}