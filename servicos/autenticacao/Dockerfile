# Etapa 1 — Compilar o projeto
FROM maven:3.9.11-amazoncorretto-21 AS build
WORKDIR /workspace

# 1. Copia o POM pai
COPY pom.xml .

# 2. Copia TODOS os arquivos pom.xml do projeto para satisfazer o Maven
# Isso mantém a estrutura de pastas necessária para o build multi-módulo
COPY servicos/autenticacao/pom.xml servicos/autenticacao/
COPY servicos/servico-agendamento/pom.xml servicos/servico-agendamento/
COPY servicos/servico-notificacao/pom.xml servicos/servico-notificacao/
COPY servicos/servico-historico/pom.xml servicos/servico-historico/

# 3. Instala o POM pai e baixa dependências (aproveita o cache do Docker)
RUN mvn install -N -DskipTests

# 4. Agora sim, copia o código-fonte do módulo que queremos buildar
COPY servicos/autenticacao/src servicos/autenticacao/src

# 5. Compila e empacota apenas a autenticacao
# -pl (project list) foca no módulo, -am (also make) garante dependências internas
RUN mvn clean package -pl servicos/autenticacao -am -DskipTests

# 6. Etapa de runtime
FROM amazoncorretto:21-alpine
WORKDIR /app

# Copia apenas o arquivo .jar (o Spring Boot gera o executável com este nome)
# O uso de --from=build garante que pegamos o resultado da etapa anterior
COPY --from=build /workspace/servicos/autenticacao/target/*.jar app.jar

# Remove o arquivo .original caso ele tenha sido copiado por engano pelo wildcard
RUN rm -f app.jar.original || true

ENTRYPOINT ["java", "-jar", "app.jar"]