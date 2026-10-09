package org.example.crudservice.exceptions;

public class CountryNotFoundException extends RuntimeException {
    public CountryNotFoundException(Long id) {
        super("Country not found with ID: " + id);
    }
}