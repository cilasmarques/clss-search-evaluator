package br.com.clss.searchevaluator.app;

import br.com.clss.searchevaluator.app.dtos.DatasetItemDTO;
import br.com.clss.searchevaluator.app.dtos.OutputDTO;
import br.com.clss.searchevaluator.app.dtos.SearchResultDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/app")
public class SearchExecutionController {

    private final JsonValidatorService jsonValidatorService;
    private final SearchDispatchService searchDispatchService;
    private final OutputService outputService;

    public SearchExecutionController(
            JsonValidatorService jsonValidatorService,
            SearchDispatchService searchDispatchService,
            OutputService outputService
    ) {
        this.jsonValidatorService = jsonValidatorService;
        this.searchDispatchService = searchDispatchService;
        this.outputService = outputService;
    }

    @PostMapping("/run")
    public List<OutputDTO> run(@RequestBody List<DatasetItemDTO> datasetItems) {
        try {
            List<DatasetItemDTO> validatedItems = jsonValidatorService.validate(datasetItems);
            List<SearchResultDTO> searchResults = searchDispatchService.executeAll(validatedItems);
            List<OutputDTO> outputs = combine(validatedItems, searchResults);
            outputService.save(outputs);
            return outputs;
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to execute search batch", e);
        }
    }

    private List<OutputDTO> combine(List<DatasetItemDTO> items, List<SearchResultDTO> results) {
        if (items.size() != results.size()) {
            throw new IllegalStateException("Dataset and result sizes do not match");
        }

        List<OutputDTO> outputs = new ArrayList<>(items.size());
        for (int i = 0; i < items.size(); i++) {
            outputs.add(new OutputDTO(items.get(i), results.get(i)));
        }
        return List.copyOf(outputs);
    }
}
