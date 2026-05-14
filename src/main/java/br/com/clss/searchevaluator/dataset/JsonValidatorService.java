package br.com.clss.searchevaluator.dataset;

import br.com.clss.searchevaluator.config.Envie;
import br.com.clss.searchevaluator.dataset.dto.SearchQueryDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class JsonValidatorService implements CommandLineRunner {

    private static final TypeReference<List<SearchQueryDTO>> QUERY_LIST_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final Envie envie;

    public JsonValidatorService(ObjectMapper objectMapper, ResourceLoader resourceLoader, Envie envie) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.envie = envie;
    }

    @Override
    public void run(String... args) {
        loadAndValidate();
    }

    public List<SearchQueryDTO> loadAndValidate() {
        String datasetPath = envie.getDatasetPath();
        if (!StringUtils.hasText(datasetPath)) {
            throw new IllegalStateException("dataset.path must not be empty");
        }

        Resource resource = resourceLoader.getResource(datasetPath);
        if (!resource.exists()) {
            throw new IllegalStateException("Dataset file not found: " + datasetPath);
        }

        List<SearchQueryDTO> queries = readQueries(resource, datasetPath);
        validateQueries(queries, datasetPath);
        return List.copyOf(queries);
    }

    private List<SearchQueryDTO> readQueries(Resource resource, String datasetPath) {
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, QUERY_LIST_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Invalid JSON in dataset file: " + datasetPath, exception);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read dataset file: " + datasetPath, exception);
        }
    }

    private void validateQueries(List<SearchQueryDTO> queries, String datasetPath) {
        if (queries == null || queries.isEmpty()) {
            throw new IllegalStateException("Dataset file must contain at least one query: " + datasetPath);
        }

        Set<String> ids = new HashSet<>();
        for (SearchQueryDTO query : queries) {
            if (query == null) {
                throw new IllegalStateException("Dataset item must not be null: " + datasetPath);
            }
            if (!StringUtils.hasText(query.id())) {
                throw new IllegalStateException("Query id must not be empty: " + datasetPath);
            }
            if (!StringUtils.hasText(query.description())) {
                throw new IllegalStateException("Query description must not be empty: " + datasetPath);
            }
            if (!ids.add(query.id())) {
                throw new IllegalStateException("Query id must be unique: " + query.id());
            }
        }
    }
}
