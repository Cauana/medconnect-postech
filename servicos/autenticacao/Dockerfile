# Etapa 1 — Build
FROM maven:3.9.11-amazoncorretto-21 AS build
WORKDIR /app

# Copiar apenas o pom.xml e baixar dependências (offline)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar o código-fonte
COPY src ./src

# Forçar annotation processing do Lombok
RUN mvn clean package -DskipTests \
    -Dmaven.compiler.showDeprecation=true \
    -Dmaven.compiler.showWarnings=true \
    -Dmaven.compiler.parameters=true \
    -Dmaven.compiler.annotationProcessorPaths=org.projectlombok:lombok:1.18.30

# Etapa 2 — Executar o jar gerado
FROM amazoncorretto:21-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
