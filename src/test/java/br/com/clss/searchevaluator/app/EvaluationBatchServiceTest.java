package br.com.clss.searchevaluator.app;

import br.com.clss.searchevaluator.app.dtos.DatasetItemDTO;
import br.com.clss.searchevaluator.app.dtos.EvaluationResultDTO;
import br.com.clss.searchevaluator.app.dtos.OutputDTO;
import br.com.clss.searchevaluator.app.dtos.SearchResultDTO;
import br.com.clss.searchevaluator.enviroment.Envie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EvaluationBatchServiceTest {

    @Test
    void evaluateAll__should_keep_processing_when_evaluation_fails() throws Exception {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("search.host", "http://localhost:8080");
        env.setProperty("search.path", "/search");
        env.setProperty("output.dir", Files.createTempDirectory("batch-output").toString());
        Envie envie = new Envie(env);

        JsonValidatorService jsonValidatorService = new JsonValidatorService(
                new com.fasterxml.jackson.databind.ObjectMapper(),
                new org.springframework.core.io.DefaultResourceLoader(),
                envie
        );

        SearchDispatchService searchDispatchService = new SearchDispatchService(envie) {
            @Override
            public SearchResultDTO execute(DatasetItemDTO query) {
                return new SearchResultDTO(query.description(), "url", 200, "{\"q\":\"" + query.id() + "\"}", null, 3L);
            }
        };

        OpenAiEvaluationService openAiEvaluationService = new OpenAiEvaluationService(
                (model, userPrompt, systemPrompt) -> {
                    if (userPrompt.contains("second")) {
                        throw new IllegalStateException("openai failure");
                    }
                    return new EvaluationResultDTO(true, 1.0, "ok");
                },
                new EvaluationPromptService(),
                envie
        );

        EvaluationBatchService service = new EvaluationBatchService(
                jsonValidatorService,
                searchDispatchService,
                openAiEvaluationService,
                new OutputService(new com.fasterxml.jackson.databind.ObjectMapper(), envie)
        );

        List<OutputDTO> outputs = service.evaluateAll(List.of(
                new DatasetItemDTO("first", "first query"),
                new DatasetItemDTO("second", "second query")
        ));

        assertThat(outputs).hasSize(2);
        assertThat(outputs.get(0).evaluationResult()).isNotNull();
        assertThat(outputs.get(1).evaluationResult()).isNull();
        assertThat(Files.list(Path.of(env.getProperty("output.dir")))).isNotEmpty();
    }
}
