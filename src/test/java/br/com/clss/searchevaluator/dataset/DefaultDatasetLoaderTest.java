package br.com.clss.searchevaluator.dataset;

import br.com.clss.searchevaluator.config.Envie;
import br.com.clss.searchevaluator.dataset.dto.DatasetItemDTO;
import br.com.clss.searchevaluator.dataset.exception.DatasetLoadException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultDatasetLoaderTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DefaultResourceLoader resourceLoader = new DefaultResourceLoader();

    @Test
    void load__should_return_valid_dataset_items() {
        DefaultDatasetLoader loader = new DefaultDatasetLoader(
                resourceLoader,
                objectMapper,
                validator,
                new Envie("classpath:dataset/queries.json")
        );

        List<DatasetItemDTO> items = loader.load();

        assertThat(items).hasSize(3);
        assertThat(items).extracting(DatasetItemDTO::id)
                .containsExactly("infra-kubernetes-aws", "seguranca-lgpd", "dados-bi");
    }

    @Test
    void load__should_fail_for_invalid_json() throws IOException {
        Path invalidJsonFile = Files.createTempFile("dataset-invalid", ".json");
        Files.writeString(invalidJsonFile, "not-json", StandardCharsets.UTF_8);

        DefaultDatasetLoader loader = new DefaultDatasetLoader(
                resourceLoader,
                objectMapper,
                validator,
                new Envie(invalidJsonFile.toUri().toString())
        );

        assertThatThrownBy(loader::load)
                .isInstanceOf(DatasetLoadException.class)
                .hasMessageContaining("Invalid dataset JSON");
    }

    @Test
    void load__should_fail_when_description_is_missing() throws IOException {
        Path missingDescriptionFile = Files.createTempFile("dataset-missing-description", ".json");
        Files.writeString(missingDescriptionFile, """
                [
                  {
                    "id": "infra-kubernetes-aws",
                    "description": null
                  }
                ]
                """, StandardCharsets.UTF_8);

        DefaultDatasetLoader loader = new DefaultDatasetLoader(
                resourceLoader,
                objectMapper,
                validator,
                new Envie(missingDescriptionFile.toUri().toString())
        );

        assertThatThrownBy(loader::load)
                .isInstanceOf(DatasetLoadException.class)
                .hasMessageContaining("description");
    }

    @Test
    void load__should_fail_when_file_is_missing() {
        Path missingFile = Path.of(System.getProperty("java.io.tmpdir"), "missing-dataset-" + System.nanoTime() + ".json");

        DefaultDatasetLoader loader = new DefaultDatasetLoader(
                resourceLoader,
                objectMapper,
                validator,
                new Envie(missingFile.toUri().toString())
        );

        assertThatThrownBy(loader::load)
                .isInstanceOf(DatasetLoadException.class)
                .hasMessageContaining("Dataset file not found");
    }
}
