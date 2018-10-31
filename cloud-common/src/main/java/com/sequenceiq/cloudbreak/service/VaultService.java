package com.sequenceiq.cloudbreak.service;

import java.security.InvalidKeyException;
import java.util.Collections;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

@Service
public class VaultService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VaultService.class);

    @Inject
    private VaultOperations operations;

    @Inject
    private VaultTemplate template;

    public void addFieldToSecret(String path, String value) throws Exception {
        long start = System.currentTimeMillis();
        VaultResponse response = operations.read(path);
        LOGGER.debug("Vault read took {} ms", System.currentTimeMillis() - start);
        if (response != null && response.getData() != null) {
            throw new InvalidKeyException(String.format("Path: %s already exists!", path));
        }
        start = System.currentTimeMillis();
        operations.write(path, Collections.singletonMap("secret", value));
        LOGGER.debug("Vault write took {} ms", System.currentTimeMillis() - start);
    }

    public String resolveSingleValue(String path) {
        if (path == null) {
            return null;
        }
        try {
            long start = System.currentTimeMillis();
            VaultResponse response = template.read(path);
            LOGGER.debug("Vault read took {} ms", System.currentTimeMillis() - start);
            if (response != null && response.getData() != null) {
                return String.valueOf(response.getData().get("secret"));
            }
        } catch (IllegalArgumentException ie) {
            LOGGER.info("Failed to fetch the secret, because it's an invalid path. Return the path.");
        } catch (Exception e) {
            LOGGER.error("Failed to fetch the secret", e);
        }
        return path;
    }

    public void deleteSecret(String path) {
        long start = System.currentTimeMillis();
        operations.delete(path);
        LOGGER.debug("Vault delete took {} ms", System.currentTimeMillis() - start);
    }

}
