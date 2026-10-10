package org.example.crudservice.services;

import com.developerhut.intothespring.ws.CountryInfoServiceSoapType;
import com.developerhut.intothespring.ws.TCountryInfo;
import org.springframework.cache.annotation.Cacheable;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.xml.ws.WebServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.crudservice.config.RedisCacheConfig;
import org.example.crudservice.dtos.Response.CountryInfoResponse;
import org.example.crudservice.dtos.Response.LookupResponse;
import org.example.crudservice.entities.CountryInfo;
import org.example.crudservice.exceptions.SoapException;
import org.example.crudservice.repository.CountryInfoRepository;
import org.example.crudservice.utility.CountryMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class IsoServiceImpl implements IsoService {

    private final CountryInfoServiceSoapType soapPort;
    private final CountryInfoRepository countryInfoRepository;
    private final CountryMapper countryMapper;
    private static final ObjectMapper MAPPER = new ObjectMapper();

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

        // log.info("SentenceCase output {}", sb.toString());

        return sb.toString();
    }


    @Override
    @Cacheable(
            cacheNames = RedisCacheConfig.COUNTRY_BY_ISO,
            key = "#result != null ? #result : 'unknown:' + #countryName",
            unless = "#result == null || #result.isBlank()"
    )
    @CircuitBreaker(name = "countrySoap", fallbackMethod = "lookupFallback")
    @Retry(name = "countrySoap")
    public String fetchIsoCodeByCountryName(String countryName) {


        String isoCode;
        try {
            isoCode = soapPort.countryISOCode(countryName);
        }
        catch (WebServiceException ex) {
            // log.warn("SOAP transport failure resolving ISO for '{}': {}", countryName, ex.getMessage());
            // rethrow so @Retry sees it and retries
            throw ex;
        }
        catch (Exception ex) {
            // SoapException is in `ignore-exceptions`, so @Retry will NOT retry.
            log.error("Unexpected SOAP fault resolving ISO for '{}': {}",
                    countryName, ex.getMessage(), ex);
            throw new SoapException(
                    "SOAP fault while resolving ISO code for '" + countryName + "'", ex);
        }

        if (isoCode == null || isoCode.isBlank()) {
            log.warn("SOAP returned empty ISO code for country '{}'", countryName);
            throw new SoapException("Unknown country: '" + countryName + "'");
        }

        // log.info("Resolved ISO code '{}' for country '{}'", isoCode, normalisedName);
        return isoCode;
    }

    @Override
    @Transactional
    public CountryInfoResponse fetchFullCountryInfoByIsoCode(String isoCode) {


        TCountryInfo soapCountry = soapPort.fullCountryInfo(isoCode);
        if (soapCountry == null) {
            throw new SoapException("FullCountryInfo returned null for ISO code: " + isoCode);
        }

        // log.debug("SOAP FullCountryInfo fetched for ISO '{}'", isoCode);
        CountryInfoResponse response = countryMapper.toResponse(soapCountry);

        if (response.getCurrencyIsoCode() != null && !response.getCurrencyIsoCode().isBlank()) {
            try {
                String currencyName = soapPort.currencyName(response.getCurrencyIsoCode());
                response.setCurrencyName(currencyName);
                // log.debug("Resolved currency name '{}' for ISO '{}'", currencyName, response.getCurrencyIsoCode());

                CountryInfo entity = countryMapper.toEntity(soapCountry);
                enrichCurrencyName(entity);

                entity = persistData(entity);
                // log.info("Persisted country '{}' (ISO='{}') with id={}",  entity.getName(), entity.getIsoCode(), entity.getId());
            }
            catch (Exception ex) {
                // log.warn("Could not resolve currency name for ISO '{}': {}", response.getCurrencyIsoCode(), ex.getMessage());
            }
        }

        // log.info("FullCountryInfo resolved iso='{}' name='{}' languages={}", response.getIsoCode(), response.getName(), response.getLanguages() == null ? 0 : response.getLanguages().size());
        return response;
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

        // Safety net for the languages
        if (incoming.getLanguages() != null) {
            incoming.getLanguages().forEach(lang -> lang.setCountry(incoming));
        }

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