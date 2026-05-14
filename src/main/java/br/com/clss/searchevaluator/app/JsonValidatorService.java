package br.com.clss.searchevaluator.app;

import br.com.clss.searchevaluator.enviroment.Envie;
import br.com.clss.searchevaluator.app.dtos.DatasetItemDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class JsonValidatorService implements CommandLineRunner {

    private static final TypeReference<List<DatasetItemDTO>> DATASET_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final Envie envie;

    public JsonValidatorService(ObjectMapper objectMapper, ResourceLoader resourceLoader, Envie envie) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.envie = envie;
    }

    @Override
    public void run(String... args) throws IOException {
        loadAndValidate();
    }

    public List<DatasetItemDTO> loadAndValidate() throws IOException {
        Resource resource = resourceLoader.getResource(envie.getDatasetPath());
        List<DatasetItemDTO> items = objectMapper.readValue(resource.getInputStream(), DATASET_TYPE);

        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("Dataset must contain at least one item");
        }

        Set<String> ids = new HashSet<>();
        for (DatasetItemDTO item : items) {
            if (item.id() == null || item.id().isBlank())
                throw new IllegalStateException("Item id must not be blank");
            if (item.description() == null || item.description().isBlank())
                throw new IllegalStateException("Item description must not be blank");
            if (!ids.add(item.id()))
                throw new IllegalStateException("Duplicate item id: " + item.id());
        }

        return List.copyOf(items);
    }
}