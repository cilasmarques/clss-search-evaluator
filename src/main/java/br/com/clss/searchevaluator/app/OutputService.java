package br.com.clss.searchevaluator.app;

import br.com.clss.searchevaluator.app.dtos.OutputDTO;
import br.com.clss.searchevaluator.enviroment.Envie;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OutputService {

    private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

    private final ObjectMapper objectMapper;
    private final Envie envie;

    public OutputService(ObjectMapper objectMapper, Envie envie) {
        this.objectMapper = objectMapper;
        this.envie = envie;
    }

    public Path save(List<OutputDTO> outputs) throws IOException {
        Path outputDir = Path.of(envie.getOutputDir());
        Files.createDirectories(outputDir);

        Path outputFile = outputDir.resolve(FILE_NAME_FORMATTER.format(LocalDateTime.now()) + ".json");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile.toFile(), outputs);
        return outputFile;
    }
}
