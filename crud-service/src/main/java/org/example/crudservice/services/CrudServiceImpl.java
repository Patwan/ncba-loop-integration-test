package org.example.crudservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.crudservice.config.RedisCacheConfig;
import org.example.crudservice.dtos.Request.CountryUpdateRequest;
import org.example.crudservice.dtos.Response.CountryResponse;
import org.example.crudservice.dtos.Response.PageResponse;
import org.example.crudservice.entities.CountryInfo;
import org.example.crudservice.entities.Language;
import org.example.crudservice.exceptions.ResourceNotFoundException;
import org.example.crudservice.repository.CountryInfoRepository;
import org.example.crudservice.utility.CountryRestMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrudServiceImpl implements CrudService {

    private final CountryInfoRepository countryInfoRepository;
    private final CountryRestMapper countryRestMapper;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = RedisCacheConfig.ALL_COUNTRIES,
            key = "'page:' + #pageable.pageNumber "
                    + "+ ':size:' + #pageable.pageSize "
                    + "+ ':sort:' + #pageable.sort.toString()"
    )
    public PageResponse<CountryResponse> findAll(Pageable pageable) {
        log.debug("Cache miss — querying DB for page {}", pageable);
        Page<CountryInfo> page = countryInfoRepository.findAll(pageable);
        return PageResponse.from(page, countryRestMapper::toResponse);
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = RedisCacheConfig.COUNTRY_BY_ID, key = "#id")
    public CountryResponse findById(Long id) {
        log.debug("Cache miss — querying DB for id {}", id);
        CountryInfo entity = countryInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Country not found with id: " + id));
        return countryRestMapper.toResponse(entity);
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {
                    RedisCacheConfig.COUNTRY_BY_ISO,
                    RedisCacheConfig.COUNTRY_BY_ID,
                    RedisCacheConfig.ALL_COUNTRIES
            },
            allEntries = true
    )
    public CountryResponse update(Long id, CountryUpdateRequest request) {
        CountryInfo entity = countryInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Country not found with id: " + id));

        countryRestMapper.updateEntityFromRequest(request, entity);

        if (request.getLanguages() != null) {
            List<Language> newLanguages = request.getLanguages().stream()
                    .map(dto -> {
                        Language lang = countryRestMapper.toLanguageEntity(dto);
                        lang.setCountry(entity);
                        return lang;
                    })
                    .toList();
            entity.replaceLanguages(newLanguages);
        }

        CountryInfo saved = countryInfoRepository.save(entity);
        log.info("Updated country id={} iso='{}'", saved.getId(), saved.getIsoCode());
        return countryRestMapper.toResponse(saved);
    }


    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {
                    RedisCacheConfig.COUNTRY_BY_ISO,
                    RedisCacheConfig.COUNTRY_BY_ID,
                    RedisCacheConfig.ALL_COUNTRIES
            },
            allEntries = true
    )
    public void delete(Long id) {
        CountryInfo entity = countryInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Country not found with id: " + id));
        countryInfoRepository.delete(entity);
        log.info("Deleted country id={} iso='{}'", id, entity.getIsoCode());
    }
}