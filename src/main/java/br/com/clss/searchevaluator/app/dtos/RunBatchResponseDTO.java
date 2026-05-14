package br.com.clss.searchevaluator.app.dtos;

public record RunBatchResponseDTO(
        int processedCount,
        String outputFile
) {
}
