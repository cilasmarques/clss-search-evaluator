package br.com.clss.searchevaluator.app;

import br.com.clss.searchevaluator.app.dtos.SearchResultDTO;
import org.springframework.stereotype.Service;

@Service
public class EvaluationPromptService {

    public String buildSystemPrompt() {
        return """
                Voce e um avaliador de relevancia para busca educacional.
                Compare a intencao da query com os resultados retornados.
                Responda APENAS com JSON valido no formato:
                {
                  "passed": boolean,
                  "score": number,
                  "reason": string
                }
                Regras:
                - Nao inclua markdown.
                - Nao inclua texto antes/depois do JSON.
                - Score entre 0 e 1.
                """;
    }

    public String buildUserPrompt(String query, SearchResultDTO searchResult) {
        return """
                Query:
                %s

                Resposta bruta do endpoint:
                %s
                """.formatted(query, searchResult.body());
    }
}
