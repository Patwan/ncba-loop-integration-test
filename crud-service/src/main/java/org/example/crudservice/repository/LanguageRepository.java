package org.example.crudservice.repository;

import org.example.crudservice.Entities.Language;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageRepository extends JpaRepository<Language, Long> {
}