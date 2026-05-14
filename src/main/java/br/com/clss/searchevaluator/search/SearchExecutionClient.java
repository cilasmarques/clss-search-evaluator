package br.com.clss.searchevaluator.search;

import br.com.clss.searchevaluator.config.Envie;
import br.com.clss.searchevaluator.dataset.dto.SearchQueryDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.net.SocketTimeoutException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchExecutionClient {

    private static final String TIMEOUT_ERROR_MESSAGE = "Timeout ao chamar o endpoint";
    private static final String COMMUNICATION_ERROR_MESSAGE = "Falha de comunicacao ao chamar o endpoint";

    private final Envie envie;
    private final HttpClient httpClient;

    public SearchExecutionClient(Envie envie) {
        this.envie = envie;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(resolveTimeout())
                .build();
    }

    public SearchExecutionResult execute(SearchQueryDTO query) {
        Instant startedAt = Instant.now();
        if (query == null) {
            return new SearchExecutionResult(
                    null,
                    null,
                    null,
                    null,
                    COMMUNICATION_ERROR_MESSAGE + ": query nula",
                    0L
            );
        }
        String url = null;
        try {
            URI uri = buildUri(query.description());
            url = uri.toString();

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(resolveTimeout())
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            String body = response.body();

            if (isSuccess(statusCode)) {
                return new SearchExecutionResult(
                        query.description(),
                        url,
                        statusCode,
                        body,
                        null,
                        elapsedMs(startedAt)
                );
            }

            return new SearchExecutionResult(
                    query.description(),
                    url,
                    statusCode,
                    body,
                    "Erro HTTP " + statusCode + " ao chamar o endpoint",
                    elapsedMs(startedAt)
            );
        } catch (java.net.http.HttpTimeoutException exception) {
            return new SearchExecutionResult(
                    query.description(),
                    url,
                    null,
                    null,
                    TIMEOUT_ERROR_MESSAGE,
                    elapsedMs(startedAt)
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new SearchExecutionResult(
                    query.description(),
                    url,
                    null,
                    null,
                    COMMUNICATION_ERROR_MESSAGE + ": " + exception.getMessage(),
                    elapsedMs(startedAt)
            );
        } catch (IOException exception) {
            return new SearchExecutionResult(
                    query.description(),
                    url,
                    null,
                    null,
                    mapCommunicationErrorMessage(exception),
                    elapsedMs(startedAt)
            );
        } catch (RuntimeException exception) {
            return new SearchExecutionResult(
                    query.description(),
                    url,
                    null,
                    null,
                    mapCommunicationErrorMessage(exception),
                    elapsedMs(startedAt)
            );
        }
    }

    public List<SearchExecutionResult> executeAll(List<SearchQueryDTO> queries) {
        if (queries == null || queries.isEmpty()) {
            return List.of();
        }

        List<SearchExecutionResult> results = new ArrayList<>(queries.size());
        for (SearchQueryDTO query : queries) {
            results.add(executeSafely(query));
        }
        return List.copyOf(results);
    }

    private SearchExecutionResult executeSafely(SearchQueryDTO query) {
        String description = query == null ? null : query.description();
        if (query == null) {
            return new SearchExecutionResult(
                    null,
                    null,
                    null,
                    null,
                    COMMUNICATION_ERROR_MESSAGE + ": query nula",
                    null
            );
        }
        try {
            return execute(query);
        } catch (RuntimeException exception) {
            return new SearchExecutionResult(
                    description,
                    description == null ? null : buildUri(description).toString(),
                    null,
                    null,
                    mapCommunicationErrorMessage(exception),
                    null
            );
        }
    }

    private URI buildUri(String description) {
        String host = requireText(envie.getSearchHost(), "search.host must not be empty");
        String path = requireText(envie.getSearchPath(), "search.path must not be empty");
        Integer durationFloor = defaultInteger(envie.getDurationFloor(), 0);
        Integer durationCeiling = defaultInteger(envie.getDurationCeiling(), 0);
        String encodedDescription = URLEncoder.encode(description, java.nio.charset.StandardCharsets.UTF_8)
                .replace("+", "%20");

        return URI.create(normalizeHost(host)
                + normalizePath(path)
                + "?description=" + encodedDescription
                + "&durationFloor=" + durationFloor
                + "&durationCeiling=" + durationCeiling);
    }

    private Duration resolveTimeout() {
        return Duration.ofMillis(defaultInteger(envie.getTimeoutMs(), 5000));
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(message);
        }
        return value;
    }

    private Integer defaultInteger(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String normalizePath(String path) {
        return path.startsWith("/") ? path : "/" + path;
    }

    private String normalizeHost(String host) {
        return host.endsWith("/") ? host.substring(0, host.length() - 1) : host;
    }

    private boolean isSuccess(int statusCode) {
        return statusCode >= HttpURLConnection.HTTP_OK && statusCode < HttpURLConnection.HTTP_MULT_CHOICE;
    }

    private Long elapsedMs(Instant startedAt) {
        return Duration.between(startedAt, Instant.now()).toMillis();
    }

    private String mapCommunicationErrorMessage(Exception exception) {
        if (exception instanceof SocketTimeoutException) {
            return TIMEOUT_ERROR_MESSAGE;
        }
        if (exception instanceof ConnectException) {
            return COMMUNICATION_ERROR_MESSAGE + ": conexão recusada";
        }
        return COMMUNICATION_ERROR_MESSAGE + ": " + exception.getMessage();
    }
}
