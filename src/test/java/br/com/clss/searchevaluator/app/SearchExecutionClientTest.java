package br.com.clss.searchevaluator.app;

import br.com.clss.searchevaluator.enviroment.Envie;
import br.com.clss.searchevaluator.app.dtos.DatasetItemDTO;
import br.com.clss.searchevaluator.app.dtos.SearchResultDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.net.http.HttpTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        SearchDispatchService client = new SearchDispatchService(createEnvie(normalizedHost(), 1500));

        SearchResultDTO result = client.execute(new DatasetItemDTO("java-basico", "Aprender Java + Spring & testes"));

        RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);

        assertThat(request).isNotNull();
        assertThat(request.getRequestUrl().host()).isEqualTo(server.getHostName());
        assertThat(request.getRequestUrl().port()).isEqualTo(server.getPort());
        assertThat(request.getPath()).isEqualTo("/search?description=Aprender%20Java%20%2B%20Spring%20%26%20testes&durationFloor=0&durationCeiling=10");
        assertThat(result.url()).isEqualTo(server.url("/search?description=Aprender%20Java%20%2B%20Spring%20%26%20testes&durationFloor=0&durationCeiling=10").toString());
    }

    @Test
    void execute__should_return_success_http_response() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"answer\":\"ok\"}"));
        server.start();

        SearchDispatchService client = new SearchDispatchService(createEnvie(normalizedHost(), 1500));

        SearchResultDTO result = client.execute(new DatasetItemDTO("java-basico", "Aprender Java do zero"));

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

        SearchDispatchService client = new SearchDispatchService(createEnvie(normalizedHost(), 1500));

        SearchResultDTO result = client.execute(new DatasetItemDTO("infra", "Pesquisar infraestrutura"));

        assertThat(result.statusHttp()).isEqualTo(404);
        assertThat(result.body()).isEqualTo("endpoint not found");
        assertThat(result.error()).isNull();
    }

    @Test
    void executeAll__should_propagate_timeout_when_one_query_times_out() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"answer\":\"ok\"}"));
        server.enqueue(new MockResponse()
                .setSocketPolicy(SocketPolicy.NO_RESPONSE));
        server.start();

        SearchDispatchService client = new SearchDispatchService(createEnvie(normalizedHost(), 100));

        assertThatThrownBy(() -> client.executeAll(List.of(
                new DatasetItemDTO("one", "Primeira consulta"),
                new DatasetItemDTO("two", "Segunda consulta")
        ))).isInstanceOf(HttpTimeoutException.class);

        RecordedRequest firstRequest = server.takeRequest(1, TimeUnit.SECONDS);
        assertThat(firstRequest).isNotNull();
        assertThat(firstRequest.getPath()).isEqualTo("/search?description=Primeira%20consulta&durationFloor=0&durationCeiling=10");
    }

    private String normalizedHost() {
        return server.url("/").toString().replaceAll("/$", "");
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
