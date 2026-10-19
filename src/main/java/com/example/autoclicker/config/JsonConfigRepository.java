package com.example.autoclicker.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;

/**
 * Repository to persist and load configurations in JSON format.
 */
public class JsonConfigRepository {

    private final ObjectMapper objectMapper;

    public JsonConfigRepository() {
        this.objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void save(AutomationConfig config, File targetFile) throws IOException {
        if (targetFile.getParentFile() != null && !targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }
        objectMapper.writeValue(targetFile, config);
    }

    public AutomationConfig load(File sourceFile) throws IOException {
        return objectMapper.readValue(sourceFile, AutomationConfig.class);
    }

    public void saveAppConfig(AppConfig config, File targetFile) throws IOException {
        if (targetFile.getParentFile() != null && !targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }
        objectMapper.writeValue(targetFile, config);
    }

    public AppConfig loadAppConfig(File sourceFile) throws IOException {
        return objectMapper.readValue(sourceFile, AppConfig.class);
    }
}
