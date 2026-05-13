package br.com.clss.searchevaluator.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "dataset")
public record DatasetProperties(
        @NotBlank String path
) {
}
