package br.com.clss.searchevaluator.config;

import br.com.clss.searchevaluator.ClssSearchEvaluatorApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ApplicationPropertiesTests {

    @Test
    void bindsApplicationPropertiesFromConfiguration() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(ClssSearchEvaluatorApplication.class)
                .web(WebApplicationType.NONE)
                .run(
                        "--search.host=http://example.com:9090",
                        "--search.path=/custom/search",
                        "--search.duration-floor=2",
                        "--search.duration-ceiling=4",
                        "--dataset.path=file:/tmp/queries.json",
                        "--output.dir=/tmp/clss-output",
                        "--openai.model=gpt-5.4"
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
    void bindsSearchHostFromEnvironmentVariable() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new SystemEnvironmentPropertySource(
                "testEnvironment",
                Map.of("SEARCH_HOST", "http://environment.example.com")
        ));

        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(ClssSearchEvaluatorApplication.class)
                .environment(environment)
                .web(WebApplicationType.NONE)
                .run()
        ) {
            SearchProperties searchProperties = context.getBean(SearchProperties.class);

            assertThat(searchProperties.host()).isEqualTo("http://environment.example.com");
        }
    }

    @Test
    void failsFastWhenRequiredConfigurationIsMissing() {
        Throwable thrown = catchThrowable(() -> new SpringApplicationBuilder(ClssSearchEvaluatorApplication.class)
                .web(WebApplicationType.NONE)
                .run(
                        "--search.host=",
                        "--search.path=/custom/search",
                        "--search.duration-floor=2",
                        "--search.duration-ceiling=4",
                        "--dataset.path=file:/tmp/queries.json",
                        "--output.dir=/tmp/clss-output",
                        "--openai.model=gpt-5.4"
                ));

        assertThat(thrown).isNotNull();
        assertThat(thrown).hasRootCauseInstanceOf(BindValidationException.class);
        assertThat(thrown).hasMessageContaining("Could not bind properties to 'SearchProperties'");
        assertThat(thrown.getCause()).hasMessageContaining("Failed to bind properties under 'search'");
        assertThat(thrown.getCause().getCause()).hasMessageContaining("must not be blank");
    }
}
