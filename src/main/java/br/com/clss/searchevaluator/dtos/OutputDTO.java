package br.com.clss.searchevaluator.dtos;

public record OutputDTO(
        DatasetItemDTO datasetItem,
        SearchResultDTO searchResult,
        EvaluationResultDTO evaluationResult
) {
}
