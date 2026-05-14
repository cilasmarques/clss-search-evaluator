package br.com.clss.searchevaluator.enviroment;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class Envie {

    private static final String DEFAULT_DATASET_PATH = "classpath:dataset/queries.json";
    private static final String DEFAULT_SEARCH_HOST = "http://localhost:8080";
    private static final String DEFAULT_SEARCH_PATH = "/search";
    private static final String DEFAULT_OPENAI_MODEL = "gpt-4.1-mini";
    private static final String DEFAULT_OUTPUT_DIR = "output";

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

    public String getOutputDir() {
        return getRequiredText("output.dir", DEFAULT_OUTPUT_DIR);
    }

    public String getOpenAiModel() {
        return getRequiredText("openai.model", DEFAULT_OPENAI_MODEL);
    }

    public String getOpenAiApiKey() {
        String apiKey = env.getProperty("openai.api-key");
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("openai.api-key must not be empty");
        }
        return apiKey;
    }

    public String getSearchAuthToken() {
        return env.getProperty("search.auth-token");
    }

    private String getRequiredText(String propertyName, String defaultValue) {
        String value = env.getProperty(propertyName, defaultValue);
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(propertyName + " must not be empty");
        }
        return value;
    }
}
