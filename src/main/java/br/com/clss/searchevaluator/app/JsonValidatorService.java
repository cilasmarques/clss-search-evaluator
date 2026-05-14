package br.com.clss.searchevaluator.app;

import br.com.clss.searchevaluator.app.dtos.DatasetItemDTO;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class JsonValidatorService {

    public List<DatasetItemDTO> validate(List<DatasetItemDTO> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("Dataset must contain at least one item");
        }

        Set<String> ids = new HashSet<>();
        for (DatasetItemDTO item : items) {
            if (item == null) {
                throw new IllegalStateException("Item must not be null");
            }
            if (item.id() == null || item.id().isBlank()) {
                throw new IllegalStateException("Item id must not be blank");
            }
            if (item.description() == null || item.description().isBlank()) {
                throw new IllegalStateException("Item description must not be blank");
            }
            if (!ids.add(item.id())) {
                throw new IllegalStateException("Duplicate item id: " + item.id());
            }
        }

        return List.copyOf(items);
    }
}
