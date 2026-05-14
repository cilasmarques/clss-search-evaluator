package br.com.clss.searchevaluator.app;

import br.com.clss.searchevaluator.enviroment.Envie;
import br.com.clss.searchevaluator.app.dtos.DatasetItemDTO;
import br.com.clss.searchevaluator.app.dtos.SearchResultDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchDispatchService {

    private static final long TIMEOUT_MS = 5_000L;
    private static final int DURATION_FLOOR = 0;
    private static final int DURATION_CEILING = 10;

    private final Envie envie;
    private final HttpClient httpClient;

    public SearchDispatchService(Envie envie) {
        this.envie = envie;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(TIMEOUT_MS))
                .build();
    }

    public SearchResultDTO execute(DatasetItemDTO query) throws IOException, InterruptedException {
        Instant startedAt = Instant.now();
        URI uri = buildUri(query.description());
        String authToken = envie.getSearchAuthToken();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofMillis(TIMEOUT_MS))
                .GET();
        if (StringUtils.hasText(authToken)) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        HttpRequest request = requestBuilder.build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return new SearchResultDTO(
                query.description(),
                uri.toString(),
                response.statusCode(),
                response.body(),
                null,
                Duration.between(startedAt, Instant.now()).toMillis()
        );
    }

    public List<SearchResultDTO> executeAll(List<DatasetItemDTO> queries) throws IOException, InterruptedException {
        if (queries == null || queries.isEmpty()) return List.of();

        List<SearchResultDTO> results = new ArrayList<>(queries.size());
        for (DatasetItemDTO query : queries) {
            results.add(execute(query));
        }
        return List.copyOf(results);
    }

    private URI buildUri(String description) {
        String encoded = URLEncoder.encode(description, StandardCharsets.UTF_8).replace("+", "%20");
        return URI.create(envie.getSearchHost() + envie.getSearchPath()
                + "?description=" + encoded
                + "&durationFloor=" + DURATION_FLOOR
                + "&durationCeiling=" + DURATION_CEILING);
    }
}
