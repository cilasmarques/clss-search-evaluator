package br.com.clss.searchevaluator.dataset;

import br.com.clss.searchevaluator.config.DatasetProperties;
import br.com.clss.searchevaluator.dataset.dto.DatasetItemDTO;
import br.com.clss.searchevaluator.dataset.exception.DatasetLoadException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class DefaultDatasetLoader implements DatasetLoader {

    private static final TypeReference<List<DatasetItemDTO>> DATASET_TYPE = new TypeReference<>() {
    };

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final DatasetProperties datasetProperties;

    public DefaultDatasetLoader(ResourceLoader resourceLoader,
                                ObjectMapper objectMapper,
                                Validator validator,
                                DatasetProperties datasetProperties) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.datasetProperties = datasetProperties;
    }

    @Override
    public List<DatasetItemDTO> load() {
        Resource resource = resourceLoader.getResource(datasetProperties.path());
        if (!resource.exists()) {
            throw new DatasetLoadException("Dataset file not found: %s".formatted(datasetProperties.path()));
        }

        List<DatasetItemDTO> items = readDataset(resource);
        validateItems(items);
        return List.copyOf(items);
    }

    private List<DatasetItemDTO> readDataset(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            List<DatasetItemDTO> items = objectMapper.readValue(inputStream, DATASET_TYPE);
            if (items == null) {
                return List.of();
            }
            return items;
        } catch (JsonProcessingException exception) {
            throw new DatasetLoadException("Invalid dataset JSON at %s".formatted(datasetProperties.path()), exception);
        } catch (IOException exception) {
            throw new DatasetLoadException("Unable to read dataset file: %s".formatted(datasetProperties.path()), exception);
        }
    }

    private void validateItems(List<DatasetItemDTO> items) {
        List<String> errors = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            DatasetItemDTO item = items.get(index);
            Set<ConstraintViolation<DatasetItemDTO>> violations = validator.validate(item);
            for (ConstraintViolation<DatasetItemDTO> violation : violations) {
                errors.add("Item %d %s".formatted(index + 1, violation.getMessage()));
            }
        }

        if (!errors.isEmpty()) {
            throw new DatasetLoadException("Dataset validation failed: %s".formatted(String.join("; ", errors)));
        }
    }
}
