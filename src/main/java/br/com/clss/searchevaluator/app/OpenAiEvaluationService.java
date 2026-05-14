package br.com.clss.searchevaluator.app;

import br.com.clss.searchevaluator.app.dtos.EvaluationResultDTO;
import br.com.clss.searchevaluator.app.dtos.SearchResultDTO;
import br.com.clss.searchevaluator.enviroment.Envie;
import org.springframework.stereotype.Service;

@Service
public class OpenAiEvaluationService {

    private final EvaluationClient evaluationClient;
    private final EvaluationPromptService evaluationPromptService;
    private final Envie envie;

    public OpenAiEvaluationService(EvaluationClient evaluationClient,
                                   EvaluationPromptService evaluationPromptService,
                                   Envie envie) {
        this.evaluationClient = evaluationClient;
        this.evaluationPromptService = evaluationPromptService;
        this.envie = envie;
    }

    public EvaluationResultDTO evaluate(String query, SearchResultDTO searchResult) {
        return evaluationClient.evaluate(
                envie.getOpenAiModel(),
                evaluationPromptService.buildUserPrompt(query, searchResult),
                evaluationPromptService.buildSystemPrompt()
        );
    }
}
