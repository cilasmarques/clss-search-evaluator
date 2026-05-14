package br.com.clss.searchevaluator.dataset;

import br.com.clss.searchevaluator.config.Envie;
import br.com.clss.searchevaluator.dataset.dto.SearchQueryDTO;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonValidatorServiceTest {

    @Test
    void loadAndValidate__should_return_queries_from_valid_dataset() {
        JsonValidatorService validator = createValidator("classpath:dataset/queries.json");

        List<SearchQueryDTO> queries = validator.loadAndValidate();

        assertThat(queries).extracting(SearchQueryDTO::id)
                .containsExactly("kubernetes-aws-microservices", "java-basico");
        assertThat(queries).extracting(SearchQueryDTO::description)
                .doesNotContainNull()
                .doesNotContain("");
    }

    @Test
    void loadAndValidate__should_fail_when_json_is_invalid() throws Exception {
        Path datasetFile = Files.createTempFile("queries", ".json");
        Files.writeString(datasetFile, "[{");

        JsonValidatorService validator = createValidator("file:" + datasetFile.toAbsolutePath());

        assertThatThrownBy(validator::loadAndValidate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid JSON in dataset file");
    }

    @Test
    void loadAndValidate__should_fail_when_description_is_empty() throws Exception {
        Path datasetFile = Files.createTempFile("queries", ".json");
        Files.writeString(datasetFile, """
                [
                  {
                    "id": "repeat-1",
                    "description": "First query"
                  },
                  {
                    "id": "repeat-2",
                    "description": ""
                  }
                ]
                """);

        JsonValidatorService validator = createValidator("file:" + datasetFile.toAbsolutePath());

        assertThatThrownBy(validator::loadAndValidate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Query description must not be empty");
    }

    @Test
    void loadAndValidate__should_fail_when_id_is_duplicated() throws Exception {
        Path datasetFile = Files.createTempFile("queries", ".json");
        Files.writeString(datasetFile, """
                [
                  {
                    "id": "repeat",
                    "description": "First query"
                  },
                  {
                    "id": "repeat",
                    "description": "Second query"
                  }
                ]
                """);

        JsonValidatorService validator = createValidator("file:" + datasetFile.toAbsolutePath());

        assertThatThrownBy(validator::loadAndValidate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Query id must be unique");
    }

    private JsonValidatorService createValidator(String datasetPath) {
        Envie envie = new Envie();
        envie.setPath(datasetPath);
        return new JsonValidatorService(
                new com.fasterxml.jackson.databind.ObjectMapper(),
                resourceLoader(),
                envie
        );
    }

    private ResourceLoader resourceLoader() {
        return new DefaultResourceLoader();
    }
}
