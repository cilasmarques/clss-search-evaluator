package br.com.clss.searchevaluator.app;

import br.com.clss.searchevaluator.app.dtos.EvaluationResultDTO;
import br.com.clss.searchevaluator.enviroment.Envie;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.StructuredResponse;
import com.openai.models.responses.StructuredResponseCreateParams;
import org.springframework.stereotype.Component;

@Component
public class OpenAiJavaEvaluationClient implements EvaluationClient {

    private final Envie envie;
    private OpenAIClient openAIClient;

    public OpenAiJavaEvaluationClient(Envie envie) {
        this.envie = envie;
    }

    @Override
    public EvaluationResultDTO evaluate(String model, String userPrompt, String systemPrompt) {
        OpenAIClient client = getClient();
        StructuredResponseCreateParams<EvaluationResultDTO> params = StructuredResponseCreateParams
                .<EvaluationResultDTO>builder()
                .model(model)
                .instructions(systemPrompt)
                .input(userPrompt)
                .text(EvaluationResultDTO.class)
                .build();

        StructuredResponse<EvaluationResultDTO> response = client.responses().create(params);

        for (var outputItem : response.output()) {
            if (!outputItem.isMessage()) {
                continue;
            }
            var message = outputItem.asMessage();
            for (var content : message.content()) {
                if (content.isOutputText()) {
                    return content.asOutputText();
                }
            }
        }

        throw new IllegalStateException("OpenAI response did not contain structured output text");
    }

    private OpenAIClient getClient() {
        if (openAIClient == null) {
            openAIClient = OpenAIOkHttpClient.builder()
                    .apiKey(envie.getOpenAiApiKey())
                    .build();
        }
        return openAIClient;
    }
}
