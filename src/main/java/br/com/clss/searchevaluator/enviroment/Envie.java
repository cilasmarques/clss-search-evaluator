package br.com.clss.searchevaluator.enviroment;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class Envie {

    private static final String DEFAULT_DATASET_PATH = "classpath:dataset/queries.json";
    private static final String DEFAULT_SEARCH_HOST = "http://localhost:8080";
    private static final String DEFAULT_SEARCH_PATH = "/search";

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

    private String getRequiredText(String propertyName, String defaultValue) {
        String value = env.getProperty(propertyName, defaultValue);
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(propertyName + " must not be empty");
        }
        return value;
    }
}
