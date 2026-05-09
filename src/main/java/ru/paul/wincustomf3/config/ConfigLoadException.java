package ru.paul.wincustomf3.config;

public final class ConfigLoadException extends Exception {

    public ConfigLoadException(final String message) {
        super(message);
    }

    public ConfigLoadException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

