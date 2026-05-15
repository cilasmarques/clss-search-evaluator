package br.com.clss.searchevaluator.app;

import br.com.clss.searchevaluator.enviroment.Envie;
import br.com.clss.searchevaluator.app.dtos.DatasetItemDTO;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.core.io.ResourceLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonValidatorServiceTest {

    @Test
    void loadAndValidate__should_return_queries_from_valid_dataset() throws Exception {
        JsonValidatorService validator = createValidator("classpath:dataset/queries.json");

        List<DatasetItemDTO> queries = validator.loadAndValidate();

        assertThat(queries).extracting(DatasetItemDTO::id)
                .containsExactly("kubernetes-aws-microservices", "java-basico");
        assertThat(queries).extracting(DatasetItemDTO::description)
                .doesNotContainNull()
                .doesNotContain("");
    }

    @Test
    void loadAndValidate__should_fail_when_json_is_invalid() throws Exception {
        Path datasetFile = Files.createTempFile("queries", ".json");
        Files.writeString(datasetFile, "[{");

        JsonValidatorService validator = createValidator("file:" + datasetFile.toAbsolutePath());

        assertThatThrownBy(validator::loadAndValidate)
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Unexpected end-of-input");
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
                .hasMessageContaining("Item description must not be blank");
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
                .hasMessageContaining("Duplicate item id: repeat");
    }

    private JsonValidatorService createValidator(String datasetPath) {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("dataset.path", datasetPath);

        Envie envie = new Envie(environment);
        return new JsonValidatorService(
                new com.fasterxml.jackson.databind.ObjectMapper(),
                resourceLoader(),
                envie
        );
    }

    private ResourceLoader resourceLoader() {
        return new DefaultResourceLoader();
    }

    @Test
    void validate__should_return_items_when_payload_is_valid() {
        JsonValidatorService validator = createValidator("classpath:dataset/queries.json");

        List<DatasetItemDTO> items = validator.validate(List.of(
                new DatasetItemDTO("id-1", "first"),
                new DatasetItemDTO("id-2", "second")
        ));

        assertThat(items).hasSize(2);
        assertThat(items).extracting(DatasetItemDTO::id).containsExactly("id-1", "id-2");
    }
}
