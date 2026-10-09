package org.example.crudservice.services;

import org.example.crudservice.config.RedisCacheConfig;
import org.example.crudservice.dtos.Response.LookupResponse;
import org.example.crudservice.dtos.Request.LookupRequest;
import org.example.crudservice.Entities.CountryInfo;
import org.example.crudservice.exceptions.CountryNotFoundException;
import org.example.crudservice.utility.CountryMapper;
import org.example.crudservice.repository.CountryInfoRepository;
import org.example.crudservice.services.PersistCountry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersistCountryImpl implements PersistCountry {

    private final CountryInfoRepository repository;
    private final CountryMapper mapper;

    /**
     * Cached: paginated list of all countries.
     * Key = page + size + sort, so different pages cache separately.
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = RedisCacheConfig.ALL_COUNTRIES,
            key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort"
    )
    public Page<LookupResponse> findAll(Pageable pageable) {
        log.debug("Cache miss — fetching page {} from DB", pageable.getPageNumber());
        return repository.findAll(pageable).map(mapper::toDto);
    }

    /**
     * Cached by ID.
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = RedisCacheConfig.COUNTRY_BY_ID, key = "#id")
    public LookupResponse findById(Long id) {
        log.debug("Cache miss — fetching country ID {} from DB", id);
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new CountryNotFoundException(id));
    }

    /**
     * Update → refresh cache for this ID + evict "all countries" cache.
     */
    @Override
    @Transactional
    @CachePut(value = RedisCacheConfig.COUNTRY_BY_ID, key = "#id")
    @CacheEvict(value = RedisCacheConfig.ALL_COUNTRIES, allEntries = true)
    public LookupResponse update(Long id, LookupRequest request) {
        CountryInfo entity = repository.findById(id)
                .orElseThrow(() -> new CountryNotFoundException(id));

        if (request.getName() != null)         entity.setName(request.getName());
        if (request.getCapitalCity() != null)  entity.setCapitalCity(request.getCapitalCity());
        if (request.getPhoneCode() != null)    entity.setPhoneCode(request.getPhoneCode());
        if (request.getCurrencyName() != null) entity.setCurrencyName(request.getCurrencyName());

        CountryInfo updated = repository.save(entity);
        log.info("Updated country ID {}", id);
        return mapper.toDto(updated);
    }

    /**
     * Delete → evict both caches.
     */
    @Override
    @Transactional
    @CacheEvict(value = {
            RedisCacheConfig.COUNTRY_BY_ID,
            RedisCacheConfig.ALL_COUNTRIES
    }, allEntries = true)
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new CountryNotFoundException(id);
        }
        repository.deleteById(id);
        log.info("Deleted country ID {}", id);
    }
}