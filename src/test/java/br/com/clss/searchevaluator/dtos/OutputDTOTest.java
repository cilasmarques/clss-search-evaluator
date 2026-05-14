package br.com.clss.searchevaluator.dtos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OutputDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void outputDTO__should_serialize_and_deserialize_full_payload() throws Exception {
        OutputDTO output = new OutputDTO(
                new DatasetItemDTO("java-basico", "Aprender Java do zero"),
                new SearchResultDTO(
                        "Aprender Java do zero",
                        "https://example.com/search?q=java",
                        200,
                        "{\"answer\":\"ok\"}",
                        null,
                        123L
                ),
                new EvaluationResultDTO(true, 0.98, "Resposta aderente ao pedido")
        );

        String json = objectMapper.writeValueAsString(output);

        assertThat(json).contains("\"datasetItem\"");
        assertThat(json).contains("\"searchResult\"");
        assertThat(json).contains("\"evaluationResult\"");
        assertThat(json).contains("\"body\":\"{\\\"answer\\\":\\\"ok\\\"}\"");

        OutputDTO readBack = objectMapper.readValue(json, OutputDTO.class);

        assertThat(readBack).isEqualTo(output);
    }

    @Test
    void outputDTO__should_preserve_search_failure_without_evaluation() throws Exception {
        OutputDTO output = new OutputDTO(
                new DatasetItemDTO("infra", "Pesquisar infraestrutura"),
                new SearchResultDTO(
                        "Pesquisar infraestrutura",
                        "https://example.com/search?q=infra",
                        null,
                        null,
                        "Timeout ao chamar o endpoint",
                        5L
                ),
                null
        );

        String json = objectMapper.writeValueAsString(output);

        assertThat(json).contains("\"error\":\"Timeout ao chamar o endpoint\"");
        assertThat(json).contains("\"evaluationResult\":null");

        OutputDTO readBack = objectMapper.readValue(json, OutputDTO.class);

        assertThat(readBack.evaluationResult()).isNull();
        assertThat(readBack.searchResult().error()).isEqualTo("Timeout ao chamar o endpoint");
        assertThat(readBack.searchResult().statusHttp()).isNull();
    }
}
