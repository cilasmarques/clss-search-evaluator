# Tasks de Implementacao - CLSS Search Evaluator

## Objetivo

Criar um projeto Java com Spring Boot e Maven para executar um dataset de queries contra um endpoint REST configuravel, salvar a resposta de cada requisicao em arquivo JSON e avaliar a qualidade de cada resposta usando um modelo da OpenAI.

Decisoes ja tomadas:

- O projeto sera uma aplicacao batch executada ao subir o Spring Boot.
- O dataset inicial ficara em JSON dentro de `src/main/resources`.
- A saida principal sera um arquivo JSON por query.
- O host do endpoint sera configuravel.
- A chave da OpenAI sera lida de `OPENAI_API_KEY`.

## Task 1 - Criar scaffold Maven/Spring Boot

Criar a estrutura inicial do projeto:

- `pom.xml`
- `mvnw` e `.mvn/wrapper`, se for usado Maven Wrapper
- `src/main/java`
- `src/main/resources`
- `src/test/java`

Dependencias previstas:

- `spring-boot-starter`
- `spring-boot-starter-web`
- `spring-boot-starter-validation`
- `spring-boot-configuration-processor`
- cliente oficial da OpenAI para Java
- `spring-boot-starter-test`
- WireMock ou MockWebServer para testes HTTP

Criterios de aceite:

- `mvn test` executa com sucesso.
- A aplicacao sobe com uma classe principal Spring Boot.
- O projeto nao exige configuracao manual fora de variaveis de ambiente e `application.yml`.

## Task 2 - Definir configuracoes da aplicacao

Criar configuracao tipada para controlar endpoint, dataset, output e OpenAI.

Configuracoes esperadas em `application.yml`:

```yaml
search:
  host: http://localhost:8080
  path: /test/learning-project/search
  duration-floor: 1
  duration-ceiling: 3

dataset:
  path: classpath:dataset/queries.json

output:
  dir: output

openai:
  model: gpt-5.4-mini
```

Criterios de aceite:

- O host pode ser sobrescrito por variavel de ambiente ou argumento Spring, por exemplo `SEARCH_HOST`.
- O diretorio de saida pode ser alterado sem recompilar.
- Falhas de configuracao devem gerar erro claro na inicializacao.

## Task 3 - Criar dataset inicial em JSON

Criar `src/main/resources/dataset/queries.json` com uma lista de queries.

Formato proposto:

```json
[
  {
    "id": "kubernetes-aws-microservices",
    "description": "Modernizar nossa infraestrutura com Kubernetes na AWS, adotando microsservicos, pipelines de CI/CD e monitoramento com logs, metricas e tracing distribuido"
  },
  {
    "id": "java-basico",
    "description": "Quero aprender Java do zero com foco em fundamentos, orientacao a objetos e boas praticas"
  }
]
```

Criterios de aceite:

- Cada item possui `id` unico e `description` nao vazio.
- O loader valida o dataset antes de executar as requisicoes.
- Erros de JSON ou campos obrigatorios ausentes interrompem a execucao com mensagem clara.

## Task 4 - Modelar os dados internos

Criar classes/records para representar o fluxo:

- `QueryDatasetItem`
- `SearchExecutionResult`
- `EvaluationResult`
- `EvaluationOutput`

Campos minimos para `SearchExecutionResult`:

- query executada
- URL chamada
- status HTTP
- body bruto
- erro, se houver
- tempo de execucao em milissegundos

Campos minimos para `EvaluationResult`:

- `passed`
- `score`
- `reason`
- `strengths`
- `problems`
- `expectedSignals`

Criterios de aceite:

- Os tipos sao serializaveis com Jackson.
- A saida final preserva resposta bruta e avaliacao estruturada.
- Nao ha perda de informacao relevante quando uma requisicao falha.

## Task 5 - Implementar loader do dataset

Implementar um componente responsavel por ler o JSON do dataset e retornar os itens validados.

Criterios de aceite:

- Leitura via `ResourceLoader` ou mecanismo equivalente do Spring.
- Suporte inicial a `classpath:dataset/queries.json`.
- Teste unitario cobrindo dataset valido, JSON invalido e item sem description.

## Task 6 - Implementar cliente REST do endpoint de busca

Criar um cliente para executar GET em:

```text
{search.host}{search.path}?description={description}&durationFloor={durationFloor}&durationCeiling={durationCeiling}
```

Cuidados:

- Fazer URL encode de `description`.
- Registrar status HTTP e body mesmo em respostas nao 2xx, quando disponivel.
- Configurar timeout.
- Nao interromper todo o batch quando uma query falhar.

Criterios de aceite:

- A URL final usa o host configurado.
- Uma falha em uma query gera `SearchExecutionResult` com erro preenchido.
- Testes cobrem encoding, sucesso HTTP, erro HTTP e timeout.

## Task 7 - Implementar analisador com OpenAI

Criar um componente que recebe a query e a resposta do endpoint e chama a OpenAI para avaliar a qualidade da resposta.

Prompt esperado:

- Instruir o modelo a agir como avaliador de relevancia de resultados de busca educacional.
- Comparar a intencao da query com a resposta retornada.
- Retornar apenas JSON valido no formato de `EvaluationResult`.

Comportamento esperado:

- Usar modelo configuravel em `openai.model`.
- Ler credencial de `OPENAI_API_KEY`.
- Tratar erro da OpenAI sem derrubar o batch inteiro.
- Em caso de erro na avaliacao, salvar o erro na saida final.

Criterios de aceite:

- A avaliacao retorna objeto estruturado.
- O parser rejeita resposta que nao seja JSON valido com erro claro.
- Testes usam mock do cliente OpenAI, sem chamar API real.

## Task 8 - Orquestrar execucao batch

Criar um `CommandLineRunner` para executar o fluxo:

1. Carregar dataset.
2. Para cada query, chamar endpoint REST.
3. Avaliar a resposta com OpenAI.
4. Salvar um JSON individual no diretorio de saida.
5. Ao final, imprimir resumo no log.

Criterios de aceite:

- A aplicacao executa todo o dataset ao subir.
- Uma query com falha nao impede as demais.
- O resumo final informa total, sucessos, falhas de endpoint e falhas de avaliacao.

## Task 9 - Persistir saidas em JSON

Criar writer para salvar cada resultado em:

```text
output/{query-id}.json
```

Regras:

- Criar o diretorio se nao existir.
- Usar JSON pretty print.
- Sanitizar `query-id` para nome de arquivo seguro.
- Sobrescrever arquivo existente da mesma query em nova execucao.

Criterios de aceite:

- Um arquivo e gerado para cada item do dataset.
- O conteudo inclui query, request, resposta bruta, avaliacao e timestamps.
- Testes cobrem criacao de diretorio e escrita do arquivo.

## Task 10 - Documentar uso do projeto

Criar `README.md` com:

- Objetivo do projeto.
- Como configurar `OPENAI_API_KEY`.
- Como configurar `SEARCH_HOST`.
- Como editar o dataset.
- Como executar a aplicacao.
- Exemplo de arquivo de saida.

Comandos esperados:

```bash
export OPENAI_API_KEY="sua-chave"
export SEARCH_HOST="http://localhost:8080"
./mvnw spring-boot:run
```

Criterios de aceite:

- Uma pessoa consegue rodar o projeto seguindo apenas o README.
- O README explica que o endpoint real precisa estar disponivel antes da execucao.

## Task 11 - Testes de integracao leves

Criar testes que validem o fluxo principal sem depender de servicos externos.

Cenarios:

- Dataset com duas queries gera dois arquivos.
- Endpoint mockado retorna sucesso e a avaliacao mockada aprova.
- Endpoint mockado retorna erro e a saida preserva o erro.
- OpenAI mockada falha e a saida preserva o resultado REST.

Criterios de aceite:

- Testes nao chamam endpoint real.
- Testes nao chamam OpenAI real.
- `mvn test` roda localmente sem credenciais.

## Task 12 - Revisao final de qualidade

Antes de considerar a implementacao concluida:

- Rodar `mvn test`.
- Rodar `mvn spring-boot:run` com endpoint mockado ou host de teste.
- Verificar arquivos em `output/`.
- Conferir que `.gitignore` ignora `target/`, `output/`, `.env`, `.idea/` e `*.iml`.
- Garantir que nenhuma chave ou segredo foi salvo no repositorio.

Criterios de aceite:

- Build e testes passam.
- Saidas sao geradas no formato esperado.
- Configuracao de host e modelo funciona sem alterar codigo.

## Ordem recomendada de implementacao

1. Scaffold Maven/Spring Boot.
2. Configuracoes tipadas.
3. Dataset JSON e loader.
4. Modelos internos.
5. Cliente REST.
6. Writer JSON.
7. Orquestrador batch.
8. Cliente/analisador OpenAI.
9. Testes unitarios e integracao leve.
10. README.

