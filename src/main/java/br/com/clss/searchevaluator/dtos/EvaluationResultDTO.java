package br.com.clss.searchevaluator.dtos;

public record EvaluationResultDTO(
        Boolean passed,
        Double score,
        String reason
) {
}
