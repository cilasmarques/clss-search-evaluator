package br.com.clss.searchevaluator.app;

import br.com.clss.searchevaluator.app.dtos.DatasetItemDTO;
import br.com.clss.searchevaluator.app.dtos.OutputDTO;
import br.com.clss.searchevaluator.app.dtos.RunBatchResponseDTO;
import br.com.clss.searchevaluator.app.dtos.SearchResultDTO;
import br.com.clss.searchevaluator.enviroment.Envie;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SearchExecutionControllerTest {

    @Test
    void run__should_execute_batch_and_return_summary() {
        DatasetItemDTO item = new DatasetItemDTO("query-1", "Java");
        SearchResultDTO result = new SearchResultDTO("Java", "http://localhost/search?description=Java", 200, "{}", null, 10L);

        StubJsonValidatorService jsonValidatorService = new StubJsonValidatorService(List.of(item), null);
        StubSearchDispatchService searchDispatchService = new StubSearchDispatchService(List.of(result), null);
        StubOutputService outputService = new StubOutputService(Path.of("output/2026-05-14-12-00-00.json"), null);

        SearchExecutionController controller = new SearchExecutionController(
                jsonValidatorService,
                searchDispatchService,
                outputService
        );

        RunBatchResponseDTO response = controller.run();

        assertThat(response.processedCount()).isEqualTo(1);
        assertThat(response.outputFile()).isEqualTo("output/2026-05-14-12-00-00.json");
    }

    @Test
    void run__should_return_bad_request_when_dataset_is_invalid() {
        StubJsonValidatorService jsonValidatorService = new StubJsonValidatorService(
                List.of(),
                new IllegalStateException("Dataset must contain at least one item")
        );
        SearchExecutionController controller = new SearchExecutionController(
                jsonValidatorService,
                new StubSearchDispatchService(List.of(), null),
                new StubOutputService(Path.of("output/out.json"), null)
        );

        assertThatThrownBy(controller::run)
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException responseStatusException = (ResponseStatusException) exception;
                    assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @Test
    void run__should_return_internal_server_error_when_dispatch_fails() {
        DatasetItemDTO item = new DatasetItemDTO("query-1", "Java");
        SearchExecutionController controller = new SearchExecutionController(
                new StubJsonValidatorService(List.of(item), null),
                new StubSearchDispatchService(List.of(), new IOException("Target unavailable")),
                new StubOutputService(Path.of("output/out.json"), null)
        );

        assertThatThrownBy(controller::run)
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException responseStatusException = (ResponseStatusException) exception;
                    assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Test
    void run__should_return_internal_server_error_when_output_write_fails() {
        DatasetItemDTO item = new DatasetItemDTO("query-1", "Java");
        SearchResultDTO result = new SearchResultDTO("Java", "http://localhost/search?description=Java", 200, "{}", null, 10L);
        SearchExecutionController controller = new SearchExecutionController(
                new StubJsonValidatorService(List.of(item), null),
                new StubSearchDispatchService(List.of(result), null),
                new StubOutputService(Path.of("output/out.json"), new IOException("Disk full"))
        );

        assertThatThrownBy(controller::run)
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException responseStatusException = (ResponseStatusException) exception;
                    assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    private static class StubJsonValidatorService extends JsonValidatorService {
        private final List<DatasetItemDTO> datasetItems;
        private final IllegalStateException failure;

        StubJsonValidatorService(List<DatasetItemDTO> datasetItems, IllegalStateException failure) {
            super(new ObjectMapper(), new DefaultResourceLoader(), createEnvie());
            this.datasetItems = datasetItems;
            this.failure = failure;
        }

        @Override
        public List<DatasetItemDTO> loadAndValidate() {
            if (failure != null) {
                throw failure;
            }
            return datasetItems;
        }
    }

    private static class StubSearchDispatchService extends SearchDispatchService {
        private final List<SearchResultDTO> searchResults;
        private final IOException failure;

        StubSearchDispatchService(List<SearchResultDTO> searchResults, IOException failure) {
            super(createEnvie());
            this.searchResults = searchResults;
            this.failure = failure;
        }

        @Override
        public List<SearchResultDTO> executeAll(List<DatasetItemDTO> queries) throws IOException {
            if (failure != null) {
                throw failure;
            }
            return searchResults;
        }
    }

    private static class StubOutputService extends OutputService {
        private final Path outputFile;
        private final IOException failure;

        StubOutputService(Path outputFile, IOException failure) {
            super(new ObjectMapper(), createEnvie());
            this.outputFile = outputFile;
            this.failure = failure;
        }

        @Override
        public Path save(List<OutputDTO> outputs) throws IOException {
            if (failure != null) {
                throw failure;
            }
            return outputFile;
        }
    }

    private static Envie createEnvie() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("dataset.path", "classpath:dataset/queries.json");
        environment.setProperty("search.host", "http://localhost:8080");
        environment.setProperty("search.path", "/search");
        environment.setProperty("output.dir", "output");
        return new Envie(environment);
    }
}
