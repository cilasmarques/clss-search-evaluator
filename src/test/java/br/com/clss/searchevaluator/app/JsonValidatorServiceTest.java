package br.com.clss.searchevaluator.app;

import br.com.clss.searchevaluator.app.dtos.DatasetItemDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonValidatorServiceTest {

    private final JsonValidatorService validator = new JsonValidatorService();

    @Test
    void validate__should_return_queries_when_payload_is_valid() {
        List<DatasetItemDTO> queries = validator.validate(List.of(
                new DatasetItemDTO("kubernetes-aws-microservices", "Query sobre infraestrutura Kubernetes na AWS"),
                new DatasetItemDTO("java-basico", "Query sobre java e springboot")
        ));

        assertThat(queries).extracting(DatasetItemDTO::id)
                .containsExactly("kubernetes-aws-microservices", "java-basico");
    }

    @Test
    void validate__should_fail_when_description_is_empty() {
        assertThatThrownBy(() -> validator.validate(List.of(
                new DatasetItemDTO("repeat-1", "First query"),
                new DatasetItemDTO("repeat-2", "")
        ))).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Item description must not be blank");
    }

    @Test
    void validate__should_fail_when_id_is_duplicated() {
        assertThatThrownBy(() -> validator.validate(List.of(
                new DatasetItemDTO("repeat", "First query"),
                new DatasetItemDTO("repeat", "Second query")
        ))).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate item id: repeat");
    }
}
