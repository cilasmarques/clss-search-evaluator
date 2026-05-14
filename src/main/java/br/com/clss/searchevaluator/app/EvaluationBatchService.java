package br.com.clss.searchevaluator.app;

import br.com.clss.searchevaluator.app.dtos.DatasetItemDTO;
import br.com.clss.searchevaluator.app.dtos.EvaluationResultDTO;
import br.com.clss.searchevaluator.app.dtos.OutputDTO;
import br.com.clss.searchevaluator.app.dtos.SearchResultDTO;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class EvaluationBatchService {

    private final JsonValidatorService jsonValidatorService;
    private final SearchDispatchService searchDispatchService;
    private final OpenAiEvaluationService openAiEvaluationService;
    private final OutputService outputService;

    public EvaluationBatchService(JsonValidatorService jsonValidatorService,
                                  SearchDispatchService searchDispatchService,
                                  OpenAiEvaluationService openAiEvaluationService,
                                  OutputService outputService) {
        this.jsonValidatorService = jsonValidatorService;
        this.searchDispatchService = searchDispatchService;
        this.openAiEvaluationService = openAiEvaluationService;
        this.outputService = outputService;
    }

    public List<OutputDTO> evaluateConfiguredDataset() throws Exception {
        return evaluateAll(jsonValidatorService.loadAndValidate());
    }

    public List<OutputDTO> evaluateAll(List<DatasetItemDTO> items) throws Exception {
        List<OutputDTO> outputs = new ArrayList<>(items.size());
        for (DatasetItemDTO item : items) {
            SearchResultDTO searchResult = executeSearch(item);
            EvaluationResultDTO evaluationResult = evaluateSafely(item.description(), searchResult);
            outputs.add(new OutputDTO(item, searchResult, evaluationResult));
        }
        List<OutputDTO> immutableOutputs = List.copyOf(outputs);
        outputService.save(immutableOutputs);
        return immutableOutputs;
    }

    private SearchResultDTO executeSearch(DatasetItemDTO item) {
        Instant startedAt = Instant.now();
        try {
            return searchDispatchService.execute(item);
        } catch (Exception e) {
            return new SearchResultDTO(
                    item.description(),
                    null,
                    null,
                    null,
                    e.getMessage(),
                    Duration.between(startedAt, Instant.now()).toMillis()
            );
        }
    }

    private EvaluationResultDTO evaluateSafely(String query, SearchResultDTO searchResult) {
        if (searchResult.error() != null) {
            return null;
        }
        try {
            return openAiEvaluationService.evaluate(query, searchResult);
        } catch (Exception e) {
            return null;
        }
    }
}
