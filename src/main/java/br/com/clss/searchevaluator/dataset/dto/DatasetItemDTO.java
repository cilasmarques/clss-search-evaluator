package br.com.clss.searchevaluator.dataset.dto;

import jakarta.validation.constraints.NotBlank;

public record DatasetItemDTO(
        @NotBlank(message = "Dataset item 'id' is required")
        String id,

        @NotBlank(message = "Dataset item 'description' is required")
        String description
) {
}
