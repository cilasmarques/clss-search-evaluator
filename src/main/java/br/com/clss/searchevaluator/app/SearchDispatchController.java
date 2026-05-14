package br.com.clss.searchevaluator.app;

import br.com.clss.searchevaluator.app.dtos.DatasetItemDTO;
import br.com.clss.searchevaluator.app.dtos.SearchResultDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/dispatch")
public class SearchDispatchController {

    private final JsonValidatorService jsonValidatorService;
    private final SearchDispatchService searchDispatchService;

    public SearchDispatchController(JsonValidatorService jsonValidatorService, SearchDispatchService searchDispatchService) {
        this.jsonValidatorService = jsonValidatorService;
        this.searchDispatchService = searchDispatchService;
    }

    @PostMapping("/queries")
    public ResponseEntity<List<SearchResultDTO>> dispatchQueries(@RequestBody List<DatasetItemDTO> queries)
            throws IOException, InterruptedException {
        List<DatasetItemDTO> validQueries = jsonValidatorService.validate(queries);
        List<SearchResultDTO> results = searchDispatchService.executeAll(validQueries);
        return ResponseEntity.ok(results);
    }
}
