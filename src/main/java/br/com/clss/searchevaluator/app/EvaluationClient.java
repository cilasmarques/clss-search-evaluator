package br.com.clss.searchevaluator.app;

import br.com.clss.searchevaluator.app.dtos.EvaluationResultDTO;

public interface EvaluationClient {
    EvaluationResultDTO evaluate(String model, String userPrompt, String systemPrompt);
}
