package org.example.crudservice.services;

import org.example.crudservice.config.RedisCacheConfig;
import org.example.crudservice.dtos.Response.LookupResponse;
import org.example.crudservice.Entities.CountryInfo;
import org.example.crudservice.Entities.Language;
import org.example.crudservice.exceptions.SoapException;
import org.example.crudservice.utility.CountryMapper;
import org.example.crudservice.repository.CountryInfoRepository;
import com.developerhut.intothespring.ws.TCountryInfo;
import com.developerhut.intothespring.ws.CountryInfoServiceSoapType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.xml.ws.WebServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountryServiceImpl implements CountryService {

    private final CountryInfoServiceSoapType soapPort;
    private final CountryInfoRepository countryInfoRepository;
    private final CountryMapper countryMapper;

    @Override
    public String convertToSentenceCase(String input) {
        if (input == null || input.isBlank()) return input;

        String trimmed = input.trim().toLowerCase();
        StringBuilder sb = new StringBuilder(trimmed.length());
        boolean capitalizeNext = true;

        for (char c : trimmed.toCharArray()) {
            if (Character.isWhitespace(c) || c == '-' || c == '_') {
                sb.append(c);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                sb.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    @CircuitBreaker(name = "countrySoap", fallbackMethod = "lookupFallback")
    @Retry(name = "countrySoap")
    @Transactional
    @CacheEvict(value = RedisCacheConfig.ALL_COUNTRY_CACHES, allEntries = true)
    public LookupResponse lookupAndPersist(String countryName) {
        String normalized = convertToSentenceCase(countryName);

        TCountryInfo soapResponse;
        try {
            soapResponse = soapPort.getCountryInfo(normalized);   // <-- check real method name
        } catch (WebServiceException e) {
            throw new SoapException("SOAP call failed for: " + normalized, e);
        }

        if (soapResponse == null || soapResponse.getSISOCode() == null
                || soapResponse.getSISOCode().isBlank()) {
            throw new SoapException("No ISO code returned for: " + normalized);
        }

        String isoCode = soapResponse.getSISOCode();

        CountryInfo entity = countryInfoRepository.findByIsoCode(isoCode)
                .orElseGet(() -> {
                    CountryInfo c = new CountryInfo();
                    c.setName(normalized);
                    c.setIsoCode(isoCode);
                    c.setLanguages(mapLanguages(soapResponse));
                    return countryInfoRepository.save(c);
                });

        return countryMapper.toLookupResponse(entity);
    }

    private List<Language> mapLanguages(TCountryInfo soap) {
        if (soap.getTLanguages() == null
                || soap.getTLanguages().getTLanguage() == null) {
            return List.of();
        }
        return soap.getTLanguages().getTLanguage().stream()
                .map(l -> {
                    Language lang = new Language();
                    lang.setIsoCode(l.getSISOCode());
                    lang.setName(l.getSName());
                    return lang;
                })
                .collect(Collectors.toList());
    }

    public LookupResponse lookupFallback(String countryName, Throwable t) {
        log.warn("SOAP lookup failed for '{}': {}", countryName, t.getMessage());
        throw new SoapException(
                "Country lookup temporarily unavailable: " + countryName, t);
    }
}