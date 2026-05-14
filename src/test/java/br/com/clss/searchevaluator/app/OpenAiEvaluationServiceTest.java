package br.com.clss.searchevaluator.app;

import br.com.clss.searchevaluator.app.dtos.EvaluationResultDTO;
import br.com.clss.searchevaluator.app.dtos.SearchResultDTO;
import br.com.clss.searchevaluator.enviroment.Envie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiEvaluationServiceTest {

    @Test
    void evaluate__should_return_structured_result_when_client_returns_result() {
        EvaluationClient evaluationClient = (model, userPrompt, systemPrompt) ->
                new EvaluationResultDTO(true, 0.9, model + "|" + userPrompt + "|" + systemPrompt);
        EvaluationPromptService promptService = new EvaluationPromptService();
        Envie envie = createEnvie();
        OpenAiEvaluationService service = new OpenAiEvaluationService(evaluationClient, promptService, envie);
        SearchResultDTO searchResult = new SearchResultDTO("query", "url", 200, "{\"items\":[]}", null, 10L);

        EvaluationResultDTO result = service.evaluate("query", searchResult);

        assertThat(result.passed()).isTrue();
        assertThat(result.score()).isEqualTo(0.9);
        assertThat(result.reason()).contains("gpt-4.1-mini");
        assertThat(result.reason()).contains("query");
    }

    private Envie createEnvie() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("openai.model", "gpt-4.1-mini");
        return new Envie(environment);
    }
}
