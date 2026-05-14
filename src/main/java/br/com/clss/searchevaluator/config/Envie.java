package br.com.clss.searchevaluator.config;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class Envie {

    private static final String DEFAULT_DATASET_PATH = "classpath:dataset/queries.json";
    private static final String DEFAULT_SEARCH_HOST = "http://localhost:8080";
    private static final String DEFAULT_SEARCH_PATH = "/search";
    private static final int DEFAULT_DURATION_FLOOR = 0;
    private static final int DEFAULT_DURATION_CEILING = 10;
    private static final int DEFAULT_TIMEOUT_MS = 5000;

    private final Environment env;

    public Envie(Environment env) {
        this.env = env;
    }

    public String getDatasetPath() {
        return getRequiredText("dataset.path", DEFAULT_DATASET_PATH);
    }

    public String getSearchHost() {
        return getRequiredText("search.host", DEFAULT_SEARCH_HOST);
    }

    public String getSearchPath() {
        return getRequiredText("search.path", DEFAULT_SEARCH_PATH);
    }

    public Integer getDurationFloor() {
        return getRequiredInteger("search.duration-floor", DEFAULT_DURATION_FLOOR);
    }

    public Integer getDurationCeiling() {
        return getRequiredInteger("search.duration-ceiling", DEFAULT_DURATION_CEILING);
    }

    public Integer getTimeoutMs() {
        return getRequiredInteger("search.timeout-ms", DEFAULT_TIMEOUT_MS);
    }

    private String getRequiredText(String propertyName, String defaultValue) {
        String value = env.getProperty(propertyName, defaultValue);
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(propertyName + " must not be empty");
        }
        return value;
    }

    private Integer getRequiredInteger(String propertyName, int defaultValue) {
        String value = env.getProperty(propertyName);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(propertyName + " must be a valid integer", exception);
        }
    }
}
