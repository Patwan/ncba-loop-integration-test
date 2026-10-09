package org.example.crudservice.repository;

import org.example.crudservice.Entities.CountryInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryInfoRepository extends JpaRepository<CountryInfo, Long> {
    Optional<CountryInfo> findByIsoCode(String isoCode);
    boolean existsByIsoCode(String isoCode);
}