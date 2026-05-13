package br.com.clss.searchevaluator.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "search")
public record SearchProperties(
        @NotBlank String host,
        @NotBlank String path,
        @Min(1) int durationFloor,
        @Min(1) int durationCeiling
) {

    @AssertTrue(message = "search.duration-floor must be less than or equal to search.duration-ceiling")
    public boolean isDurationRangeValid() {
        return durationFloor <= durationCeiling;
    }
}
