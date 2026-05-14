package br.com.clss.searchevaluator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

class ClssSearchEvaluatorApplicationTests {

    @Test
    void contextLoads() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(ClssSearchEvaluatorApplication.class)
                .web(WebApplicationType.NONE)
                .run()) {
        }
    }
}
