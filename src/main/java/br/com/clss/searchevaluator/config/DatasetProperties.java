package br.com.clss.searchevaluator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dataset")
public record DatasetProperties(String path) {
}
