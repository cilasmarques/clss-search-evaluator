package br.com.clss.searchevaluator.app;

import br.com.clss.searchevaluator.app.dtos.OutputDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OutputFileService {

    private static final DateTimeFormatter FILE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final ObjectMapper objectMapper;
    private final Path outputDir;

    public OutputFileService(ObjectMapper objectMapper, @Value("${output.dir}") String outputDir) {
        this.objectMapper = objectMapper;
        this.outputDir = Path.of(outputDir);
    }

    public void write(List<OutputDTO> outputs) throws IOException {
        Files.createDirectories(outputDir);
        String fileName = "evaluation-result-" + FILE_FORMAT.format(LocalDateTime.now()) + ".json";
        Path filePath = outputDir.resolve(fileName);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), outputs);
    }
}
