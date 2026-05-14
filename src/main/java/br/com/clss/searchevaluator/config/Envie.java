package br.com.clss.searchevaluator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Envie {

    public static final String SPRING_APPLICATION_NAME = "spring.application.name";
    public static final String SEARCH_HOST = "search.host";
    public static final String DATASET_PATH = "dataset.path";
    public static final String OUTPUT_DIR = "output.dir";

    private final String datasetPath;

    public Envie(@Value("${" + DATASET_PATH + ":classpath:dataset/queries.json}") String datasetPath) {
        this.datasetPath = datasetPath;
    }

    public String datasetPath() {
        return datasetPath;
    }
}
