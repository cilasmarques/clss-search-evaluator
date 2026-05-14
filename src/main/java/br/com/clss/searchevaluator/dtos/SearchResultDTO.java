package br.com.clss.searchevaluator.dtos;

public record SearchResultDTO(
        String query,
        String url,
        Integer statusHttp,
        String body,
        String error,
        Long executionTimeMs
) {
}
