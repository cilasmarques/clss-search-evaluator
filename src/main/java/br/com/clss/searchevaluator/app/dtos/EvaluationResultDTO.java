package br.com.clss.searchevaluator.app.dtos;

public record EvaluationResultDTO(
        Boolean passed,
        Double score,
        String reason
) {
}
