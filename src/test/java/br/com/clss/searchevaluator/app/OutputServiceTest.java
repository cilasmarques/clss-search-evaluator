package br.com.clss.searchevaluator.app;

import br.com.clss.searchevaluator.app.dtos.DatasetItemDTO;
import br.com.clss.searchevaluator.app.dtos.OutputDTO;
import br.com.clss.searchevaluator.app.dtos.SearchResultDTO;
import br.com.clss.searchevaluator.enviroment.Envie;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OutputServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void save__should_create_output_directory_and_write_file_with_timestamp_name() throws Exception {
        Path tempDir = Files.createTempDirectory("output-service-test");
        String outputDir = tempDir.resolve("output").toString();
        OutputService outputService = new OutputService(objectMapper, createEnvie(outputDir));

        Path savedFile = outputService.save(List.of(sampleOutput()));

        assertThat(savedFile.getParent()).isEqualTo(Path.of(outputDir));
        assertThat(Files.exists(savedFile.getParent())).isTrue();
        assertThat(savedFile.getFileName().toString())
                .matches("\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}\\.json");
        assertThat(Files.exists(savedFile)).isTrue();
    }

    @Test
    void save__should_write_json_with_expected_fields() throws Exception {
        Path tempDir = Files.createTempDirectory("output-service-fields-test");
        OutputService outputService = new OutputService(
                objectMapper,
                createEnvie(tempDir.resolve("output").toString())
        );

        Path savedFile = outputService.save(List.of(sampleOutput()));
        JsonNode root = objectMapper.readTree(savedFile.toFile());

        assertThat(root.isArray()).isTrue();
        assertThat(root).hasSize(1);
        JsonNode item = root.get(0);
        assertThat(item.has("datasetItem")).isTrue();
        assertThat(item.has("searchResult")).isTrue();
        assertThat(item.has("evaluationResult")).isFalse();
        assertThat(item.path("datasetItem").path("id").asText()).isEqualTo("query-1");
        assertThat(item.path("searchResult").path("statusHttp").asInt()).isEqualTo(200);
    }

    private Envie createEnvie(String outputDir) {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("output.dir", outputDir);
        return new Envie(environment);
    }

    private OutputDTO sampleOutput() {
        DatasetItemDTO datasetItem = new DatasetItemDTO("query-1", "Search for java");
        SearchResultDTO searchResult = new SearchResultDTO(
                "Search for java",
                "http://localhost:8080/search?description=Search%20for%20java",
                200,
                "{\"answer\":\"ok\"}",
                null,
                100L
        );
        return new OutputDTO(datasetItem, searchResult);
    }
}
