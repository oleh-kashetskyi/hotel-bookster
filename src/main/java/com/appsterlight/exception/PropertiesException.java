package com.appsterlight.exception;

public class PropertiesException extends GeneralException {
    public PropertiesException(String message) {
        super(message);
    }

    public PropertiesException(String message, Throwable cause) {
        super(message, cause);
    }
}
