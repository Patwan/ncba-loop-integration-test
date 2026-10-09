package org.example.crudservice.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "country_info",
        uniqueConstraints = @UniqueConstraint(columnNames = "iso_code"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iso_code", nullable = false, unique = true, length = 2)
    private String isoCode;

    @Column(nullable = false)
    private String name;

    @Column(name = "capital_city")
    private String capitalCity;

    @Column(name = "phone_code")
    private String phoneCode;

    @Column(name = "continent_code", length = 2)
    private String continentCode;

    @Column(name = "currency_iso_code", length = 3)
    private String currencyIsoCode;

    @Column(name = "currency_name")
    private String currencyName;

    @Column(name = "flag_url")
    private String flagUrl;

    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Language> languages = new ArrayList<>();

    public void addLanguage(Language language) {
        languages.add(language);
        language.setCountry(this);
    }

    public void removeLanguage(Language language) {
        languages.remove(language);
        language.setCountry(null);
    }
}