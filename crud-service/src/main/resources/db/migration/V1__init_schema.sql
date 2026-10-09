CREATE TABLE country_info (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              iso_code VARCHAR(2) NOT NULL UNIQUE,
                              name VARCHAR(255) NOT NULL,
                              capital_city VARCHAR(255),
                              phone_code VARCHAR(20),
                              continent_code VARCHAR(2),
                              currency_iso_code VARCHAR(3),
                              currency_name VARCHAR(100),
                              flag_url VARCHAR(500)
);

CREATE TABLE language (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          iso_code VARCHAR(3),
                          name VARCHAR(255) NOT NULL,
                          country_info_id BIGINT NOT NULL,
                          CONSTRAINT fk_language_country
                              FOREIGN KEY (country_info_id) REFERENCES country_info(id)
                                  ON DELETE CASCADE
);

CREATE INDEX idx_country_iso ON country_info(iso_code);
CREATE INDEX idx_language_country ON language(country_info_id);