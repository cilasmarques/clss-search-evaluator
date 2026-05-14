package br.com.clss.searchevaluator.app.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OutputDTO(
        DatasetItemDTO datasetItem,
        SearchResultDTO searchResult,
        EvaluationResultDTO evaluationResult
) {
    public OutputDTO(DatasetItemDTO datasetItem, SearchResultDTO searchResult) {
        this(datasetItem, searchResult, null);
    }
}
