package br.com.clss.searchevaluator.config;

import br.com.clss.searchevaluator.ClssSearchEvaluatorApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ApplicationPropertiesTests {

    @Test
    void bindsApplicationPropertiesFromConfiguration() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(ClssSearchEvaluatorApplication.class)
                .web(WebApplicationType.NONE)
                .run(
                        "--" + Envie.SEARCH_HOST + "=http://example.com:9090",
                        "--" + Envie.SEARCH_PATH + "=/custom/search",
                        "--" + Envie.SEARCH_DURATION_FLOOR + "=2",
                        "--" + Envie.SEARCH_DURATION_CEILING + "=4",
                        "--" + Envie.DATASET_PATH + "=file:/tmp/queries.json",
                        "--" + Envie.OUTPUT_DIR + "=/tmp/clss-output",
                        "--" + Envie.OPENAI_MODEL + "=gpt-5.4"
                )
        ) {
            SearchProperties searchProperties = context.getBean(SearchProperties.class);
            DatasetProperties datasetProperties = context.getBean(DatasetProperties.class);
            OutputProperties outputProperties = context.getBean(OutputProperties.class);
            OpenAiProperties openAiProperties = context.getBean(OpenAiProperties.class);

            assertThat(searchProperties.host()).isEqualTo("http://example.com:9090");
            assertThat(searchProperties.path()).isEqualTo("/custom/search");
            assertThat(searchProperties.durationFloor()).isEqualTo(2);
            assertThat(searchProperties.durationCeiling()).isEqualTo(4);
            assertThat(datasetProperties.path()).isEqualTo("file:/tmp/queries.json");
            assertThat(outputProperties.dir()).isEqualTo("/tmp/clss-output");
            assertThat(openAiProperties.model()).isEqualTo("gpt-5.4");
        }
    }

    @Test
    void failsFastWhenRequiredConfigurationIsMissing() {
        Throwable thrown = catchThrowable(() -> new SpringApplicationBuilder(ClssSearchEvaluatorApplication.class)
                .web(WebApplicationType.NONE)
                .run(
                        "--" + Envie.SEARCH_HOST + "=",
                        "--" + Envie.SEARCH_PATH + "=/custom/search",
                        "--" + Envie.SEARCH_DURATION_FLOOR + "=2",
                        "--" + Envie.SEARCH_DURATION_CEILING + "=4",
                        "--" + Envie.DATASET_PATH + "=file:/tmp/queries.json",
                        "--" + Envie.OUTPUT_DIR + "=/tmp/clss-output",
                        "--" + Envie.OPENAI_MODEL + "=gpt-5.4"
                ));

        assertThat(thrown).isNotNull();
        assertThat(thrown).hasRootCauseInstanceOf(BindValidationException.class);
        assertThat(thrown).hasMessageContaining("Could not bind properties to 'SearchProperties'");
        assertThat(thrown.getCause()).hasMessageContaining("Failed to bind properties under 'search'");
        assertThat(thrown.getCause().getCause()).hasMessageContaining("must not be blank");
    }
}
