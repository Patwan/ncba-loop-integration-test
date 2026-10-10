package org.example.crudservice.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "language",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_language_country_iso",
                columnNames = {"country_info_id", "iso_code"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "country")
public class Language {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Include
    @Column(name = "iso_code", length = 3)
    private String isoCode;

    @Column(name = "language_name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "country_info_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_language_country_info")
    )
    private CountryInfo country;
}