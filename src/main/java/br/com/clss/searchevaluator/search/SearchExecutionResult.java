package br.com.clss.searchevaluator.search;

public record SearchExecutionResult(
        String query,
        String url,
        Integer statusHttp,
        String body,
        String error,
        Long executionTimeMs
) {
}
