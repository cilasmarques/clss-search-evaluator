FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw -B -DskipTests dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/clss-search-evaluator-0.0.1-SNAPSHOT.jar /app/app.jar

ENV OUTPUT_DIR=output
ENV DATASET_PATH=classpath:dataset/queries.json
ENV SEARCH_HOST=http://host.docker.internal:8080
ENV SEARCH_PATH=/search

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
