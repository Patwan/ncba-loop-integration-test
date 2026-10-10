package org.example.crudservice.services;

import com.developerhut.intothespring.ws.CountryInfoServiceSoapType;
import com.developerhut.intothespring.ws.TCountryInfo;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.crudservice.config.RedisCacheConfig;
import org.example.crudservice.dtos.Response.LookupResponse;
import org.example.crudservice.entities.CountryInfo;
import org.example.crudservice.exceptions.SoapException;
import org.example.crudservice.repository.CountryInfoRepository;
import org.example.crudservice.utility.CountryMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IsoServiceImpl implements IsoService {

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
    @CacheEvict(
            cacheNames = {
                    RedisCacheConfig.COUNTRY_BY_ISO,
                    RedisCacheConfig.COUNTRY_BY_ID,
                    RedisCacheConfig.ALL_COUNTRIES
            },
            allEntries = true
    )
    public LookupResponse fetchIsoCodeByCountryName(String countryName) {

        if (countryName == null || countryName.isBlank()) {
            throw new SoapException("Country name must not be null or blank");
        }
        final String normalisedName = convertToSentenceCase(countryName);

        String isoCode = soapPort.countryISOCode(normalisedName);

        if (isoCode == null || isoCode.isBlank()) {
            throw new SoapException("No ISO code returned for country: " + normalisedName);
        }
        log.debug("Resolved ISO code '{}' for country '{}'", isoCode, normalisedName);

        TCountryInfo soapCountry = soapPort.fullCountryInfo(isoCode);

        if (soapCountry == null) {
            throw new SoapException("FullCountryInfo returned null for ISO code: " + isoCode);
        }
        log.debug("Fetched full country info for ISO code '{}': name='{}', currencyIso='{}'",
                isoCode, soapCountry.getSName(), soapCountry.getSCurrencyISOCode());


        CountryInfo entity = countryMapper.toEntity(soapCountry);


        enrichCurrencyName(entity);

        entity = persistData(entity);
        log.info("Persisted country '{}' (ISO='{}') with id={}",
                entity.getName(), entity.getIsoCode(), entity.getId());
        return countryMapper.toLookupResponse(entity);
    }

    private void enrichCurrencyName(CountryInfo entity) {
        String currencyIso = entity.getCurrencyIsoCode();
        if (currencyIso == null || currencyIso.isBlank()) {
            log.debug("Skipping currency-name enrichment: no ISO code on entity '{}'",
                    entity.getIsoCode());
            return;
        }

        try {
            String currencyName = soapPort.currencyName(currencyIso);
            if (currencyName == null || currencyName.isBlank()) {
                log.warn("CurrencyName endpoint returned blank for ISO code '{}'", currencyIso);
                return;
            }
            entity.setCurrencyName(currencyName);
            log.debug("Resolved currency name '{}' for ISO code '{}'", currencyName, currencyIso);
        } catch (Exception ex) {
            log.warn("Could not resolve currency name for ISO code '{}': {}",
                    currencyIso, ex.getMessage());
        }
    }

    private CountryInfo persistData(CountryInfo incoming) {
        return countryInfoRepository.findByIsoCode(incoming.getIsoCode())
                .map(existing -> {
                    existing.setName(incoming.getName());
                    existing.setCapitalCity(incoming.getCapitalCity());
                    existing.setCurrencyIsoCode(incoming.getCurrencyIsoCode());
                    existing.setCurrencyName(incoming.getCurrencyName());
                    existing.setPhoneCode(incoming.getPhoneCode());
                    existing.setContinentCode(incoming.getContinentCode());
                    existing.setFlagUrl(incoming.getFlagUrl());
                    existing.replaceLanguages(incoming.getLanguages());
                    return countryInfoRepository.save(existing);
                })
                .orElseGet(() -> countryInfoRepository.save(incoming));
    }


    @SuppressWarnings("unused")
    private LookupResponse lookupFallback(String countryName, Throwable ex) {
        log.error("SOAP lookup failed for country '{}': {}", countryName, ex.getMessage(), ex);
        throw new SoapException(
                "Unable to retrieve country information for '" + countryName + "'. Please try again later.",
                ex
        );
    }
}