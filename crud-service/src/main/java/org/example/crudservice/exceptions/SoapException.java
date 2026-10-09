package org.example.crudservice.exceptions;

public class SoapException extends RuntimeException {
    public SoapException(String message) {
        super(message);
    }
    public SoapException(String message, Throwable cause) {
        super(message, cause);
    }
}