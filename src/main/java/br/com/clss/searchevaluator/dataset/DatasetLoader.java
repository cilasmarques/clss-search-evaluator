package br.com.clss.searchevaluator.dataset;

import br.com.clss.searchevaluator.dataset.dto.DatasetItemDTO;

import java.util.List;

public interface DatasetLoader {

    List<DatasetItemDTO> load();
}
