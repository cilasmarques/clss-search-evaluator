package br.com.clss.searchevaluator.search;

import br.com.clss.searchevaluator.config.Envie;
import br.com.clss.searchevaluator.dataset.dto.SearchQueryDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class SearchExecutionClientTest {

    private MockWebServer server;

    @BeforeEach
    void setUp() {
        server = new MockWebServer();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void execute__should_use_configured_host_and_encode_description() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"answer\":\"ok\"}"));
        server.start();

        SearchExecutionClient client = new SearchExecutionClient(createEnvie(server.url("/").toString(), 1500));

        SearchExecutionResult result = client.execute(new SearchQueryDTO("java-basico", "Aprender Java + Spring & testes"));

        RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);

        assertThat(request).isNotNull();
        assertThat(request.getRequestUrl().host()).isEqualTo(server.getHostName());
        assertThat(request.getRequestUrl().port()).isEqualTo(server.getPort());
        assertThat(request.getPath()).isEqualTo("/search?description=Aprender%20Java%20%2B%20Spring%20%26%20testes&durationFloor=1&durationCeiling=12");
        assertThat(result.url()).isEqualTo(server.url("/search?description=Aprender%20Java%20%2B%20Spring%20%26%20testes&durationFloor=1&durationCeiling=12").toString());
    }

    @Test
    void execute__should_return_success_http_response() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"answer\":\"ok\"}"));
        server.start();

        SearchExecutionClient client = new SearchExecutionClient(createEnvie(server.url("/").toString(), 1500));

        SearchExecutionResult result = client.execute(new SearchQueryDTO("java-basico", "Aprender Java do zero"));

        assertThat(result.statusHttp()).isEqualTo(200);
        assertThat(result.body()).isEqualTo("{\"answer\":\"ok\"}");
        assertThat(result.error()).isNull();
        assertThat(result.executionTimeMs()).isNotNull();
        assertThat(result.executionTimeMs()).isGreaterThanOrEqualTo(0L);
    }

    @Test
    void execute__should_return_http_error_with_body() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(404).setBody("endpoint not found"));
        server.start();

        SearchExecutionClient client = new SearchExecutionClient(createEnvie(server.url("/").toString(), 1500));

        SearchExecutionResult result = client.execute(new SearchQueryDTO("infra", "Pesquisar infraestrutura"));

        assertThat(result.statusHttp()).isEqualTo(404);
        assertThat(result.body()).isEqualTo("endpoint not found");
        assertThat(result.error()).contains("Erro HTTP 404");
    }

    @Test
    void execute__should_return_timeout_error_when_server_is_slow() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeadersDelay(1, TimeUnit.SECONDS));
        server.start();

        SearchExecutionClient client = new SearchExecutionClient(createEnvie(server.url("/").toString(), 100));

        SearchExecutionResult result = client.execute(new SearchQueryDTO("infra", "Timeout esperado"));

        assertThat(result.statusHttp()).isNull();
        assertThat(result.body()).isNull();
        assertThat(result.error()).isEqualTo("Timeout ao chamar o endpoint");
    }

    @Test
    void executeAll__should_continue_when_one_query_times_out() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"answer\":\"ok\"}"));
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeadersDelay(1, TimeUnit.SECONDS));
        server.start();

        SearchExecutionClient client = new SearchExecutionClient(createEnvie(server.url("/").toString(), 100));

        List<SearchExecutionResult> results = client.executeAll(List.of(
                new SearchQueryDTO("one", "Primeira consulta"),
                new SearchQueryDTO("two", "Segunda consulta")
        ));

        assertThat(results).hasSize(2);
        assertThat(results.get(0).error()).isNull();
        assertThat(results.get(1).error()).isEqualTo("Timeout ao chamar o endpoint");
    }

    private Envie createEnvie(String host, int timeoutMs) {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("search.host", host);
        environment.setProperty("search.path", "/search");
        environment.setProperty("search.duration-floor", "1");
        environment.setProperty("search.duration-ceiling", "12");
        environment.setProperty("search.timeout-ms", Integer.toString(timeoutMs));
        return new Envie(environment);
    }
}
