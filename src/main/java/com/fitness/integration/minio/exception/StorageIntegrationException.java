package com.fitness.integration.minio.exception;

import com.fitness.integration.common.exception.IntegrationException;

public class StorageIntegrationException extends IntegrationException {

    public StorageIntegrationException(String message) {
        super(message);
    }

    public StorageIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
