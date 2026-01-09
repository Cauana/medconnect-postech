# 🍽️ Med Connect Services

## 🧭 Visão Geral do Projeto

O **Med Connect Services** é um ecossistema distribuído com três microserviços responsáveis pela automatização de um ambiente hopistalar: O **serviço de autenticação** responsável por garantir que cada tipo de usuário tenha acesso controlado às funcionalidades, o **serviço de agendamento** responsável por garantir o agendamento eficaz de consultas e também responsável pelo gerenciamento do histórico de pacientes e  **serviço de notificação** responsável pelo envio de lembretes automáticos para garantir a presença de pacientes nas consultas.  
Objetivo Central: Estabelecer uma arquitetura multi-tenant capaz de orquestrar múltiplos estabelecimentos hospitalares de forma centralizada, garantindo o isolamento de dados e a independência de escala. A estrutura foi projetada para ser extensível, permitindo a acoplagem de novos microsserviços — como Gestão de Suprimentos (Pedidos) e Experiência do Paciente (Avaliações) — sem impactar a disponibilidade dos serviços de Agendamento, Histórico e Notificação já existentes.

📦 **Repositório GitHub:** [Cauana/MedConnect](https://github.com/Cauana/medconnect-postech)

---
Grupo:
- Cauana Dias Costa 
- Ivan Domingos Moreira 
- Jociel Alves de Jesus
- Wellington Feijó da Costa

## ⚙️ Infraestrutura do Projeto

### 🗂️ Estrutura de Pastas

| Caminho                               | Descrição                                                |
|---------------------------------------|----------------------------------------------------------|
| `servicos`                            | Microservicos separados por responsabilidades            |
| `servicos/autenticacao`               | Estrutura do microservico de autenticacao                |
| `servicos/servico-agendamento`        | Estrutura do microservico de agendamento                 |
| `servicos/servico-notificacao`        | Estrutura do microservico de notificação                 |
| `src/main/java/`                      | Código-fonte principal (Java)                            |
| `src/main/resources/`                 | Arquivos de configuração, scripts SQL, propriedades      |
| `src/test/java/`                      | Testes automatizados                                     |
| `target/`                             | Artefatos gerados pelo Maven (build)                     |
| `.env`                                | Variáveis de ambiente para banco de dados                |
| `docker-compose.yml`                  | Orquestração dos containers (aplicação e banco de dados) |
| `Dockerfile`                          | Build da imagem da aplicação                             |
| `pom.xml`                             | Gerenciamento de dependências Maven                      |
| `collection tech challenge clean arq` | Collection com testes de integração (Insomnia)           |

## Arquitetura

- Autenticação (porta host 8080) — geração de JWT e gestão de usuários de autenticação
- Agendamento (porta host 8081) — CRUD de consultas e perfis locais do serviço
- Notificação (porta host 8082) — consumo de eventos e envio de e-mails via SMTP (Mailtrap/Gmail configurável)
- Histórico (porta host 8083) — GraphQL para histórico detalhado de consultas
- PostgreSQL (porta host 5432) — banco compartilhado
- Kafka + Zookeeper — mensageria entre serviços

As portas expostas no host são definidas em [`docker-compose.yaml`](file:///e:/Fase3/medconnect-postech/docker-compose.yaml).

---

### 🗄️ Banco de Dados

- **Banco:** PostgreSQL rodando em container Docker

### 💻 Backend

- **Linguagem:** Java 21
- **Framework:** Spring Boot
- **Persistência:** Spring Data JPA
- **Utilitários:** Lombok
- **Interface de Dados (API):** GraphQL
- **Mensageria:** Apache Kafka
- **Autenticação:** JWT (JSON Web Token) / OAuth2
- **Containerização:** Docker & Docker Compose
- **Documentação:** Swagger/OpenAPI (acesso via `/swagger-ui.html`)

#### Execução do Projeto

1. Configure variáveis no arquivo `.env` na raiz:
    - JWT_SECRET
    - EMAIL_HOST, EMAIL_PORT, EMAIL_USER, EMAIL_PASS (SMTP; por padrão, Mailtrap)
    - SPRING perfis e URLs conforme necessário
2. Execute:

```bash
docker compose up --build -d
```

3. Acesse os serviços:

- Autenticação: http://localhost:8080
- Agendamento: http://localhost:8081
- Notificação: http://localhost:8082
- Histórico (GraphQL): http://localhost:8083

---
## 🌐 Endpoints da API

## Fluxo de Autenticação

Endpoints principais em Autenticação (host 8080):

- POST `/auth/login` — corpo: `{ "usuario": "<login>", "senha": "<senha>" }`  
  Resposta: `{ "token": "<JWT>" }`
- POST `/auth/register` — cria usuário (PACIENTE/MEDICO/ENFERMEIRO)

O token inclui claims `role` e `userId`, compatíveis com a validação em serviços downstream.

## Serviço de Agendamento

Porta host: 8081

Endpoints REST:

- POST `/usuarios` — cria perfil local do serviço (requer ADMIN).  
  Corpo exemplo:

```json
{
  "nome": "Dr. João",
  "cpf": "11111111111",
  "email": "medico1@example.com",
  "dataNascimento": "1980-01-01",
  "tipo": "MEDICO"
}
```

- GET `/usuarios` — lista perfis (ADMIN/MEDICO/ENFERMEIRO)
- POST `/consultas` — cria consulta (ADMIN/MEDICO/ENFERMEIRO). Corpo:

```json
{
  "idPaciente": 3,
  "idMedico": 1,
  "dataHora": "2026-02-20T14:30:00",
  "observacoes": "Criada pelo enfermeiro"
}
```

- GET `/consultas` — lista, com controle de acesso por papel
- GET `/consultas/{id}` — busca com controle de acesso
- PUT `/consultas/{id}/status` — altera status (ADMIN/MEDICO/ENFERMEIRO). Corpo:

```json
{ "status": "CONFIRMADA" }
```

- PUT `/consultas/{id}` — edita consulta (regras por papel)
- DELETE `/consultas/{id}` — remove consulta (ADMIN/MEDICO/ENFERMEIRO)

Validações:

- `idPaciente` e `idMedico` devem existir na tabela `usuarios` do serviço de agendamento.
- Histórico é registrado automaticamente em cada ação.

Implementações relevantes:

- Controller: [`ConsultaController`](file:///e:/Fase3/medconnect-postech/servicos/servico-agendamento/src/main/java/com/adjt/medconnect/servicoagendamento/controller/ConsultaController.java)
- Serviço: [`ConsultaService`](file:///e:/Fase3/medconnect-postech/servicos/servico-agendamento/src/main/java/com/adjt/medconnect/servicoagendamento/service/ConsultaService.java)

## Serviço de Histórico (GraphQL)

Porta host: 8083

Schema e resolvers:

- Consultas por ID, por paciente, por médico e por período  
  Veja [`schema.graphqls`](file:///e:/Fase3/medconnect-postech/servicos/servico-agendamento/src/main/resources/graphql/schema.graphqls) e [`HistoricoConsultaResolver`](file:///e:/Fase3/medconnect-postech/servicos/servico-agendamento/src/main/java/com/adjt/medconnect/servicoagendamento/graphql/HistoricoConsultaResolver.java).

Exemplo de query resumida:

```graphql
query {
  historicoConsultaResumo(idConsulta: 1) {
    id
    idConsulta
    statusNovo
    dataAlteracao
  }
}
```

## Serviço de Notificação

Porta host: 8082. Consome eventos de agendamento via Kafka e envia e-mails (SMTP configurável).  
Configuração em [`application.yml`](file:///e:/Fase3/medconnect-postech/servicos/servico-notificacao/src/main/resources/application.yml).

- Kafka
    - Tópico: `agendamento-topic`
    - Evento: `ConsultaEvent` com campos:
        - idConsulta, emailPaciente, nomePaciente, nomeMedico, dataHora, status
        - agendadoPorRole (MEDICO/ENFERMEIRO), agendadoPorNome
    - Publicação do evento no agendamento: veja [`ConsultaService`](file:///e:/Fase3/medconnect-postech/servicos/servico-agendamento/src/main/java/com/adjt/medconnect/servicoagendamento/service/ConsultaService.java#L170-L206)
    - Consumo e envio de e-mail: veja [`NotificacaoConsumer`](file:///e:/Fase3/medconnect-postech/servicos/servico-notificacao/src/main/java/com/adjt/medconnect/serviconotificacao/kafka/NotificacaoConsumer.java)

- SMTP
    - Variáveis suportadas: EMAIL_HOST, EMAIL_PORT, EMAIL_USER, EMAIL_PASS
    - Padrão: Mailtrap (sandbox). Para enviar e-mails reais, use Gmail com App Password (2FA).
    - Remetente definido por `spring.mail.username` (EMAIL_USER)

- Redação do e-mail
    - Médico: “Sua consulta com {nomeMedico} foi agendada pelo médico {agendadoPorNome} para {dd/MM/yyyy HH:mm}.”
    - Enfermeiro: “Sua consulta com {nomeMedico} foi agendada pelo enfermeiro {agendadoPorNome} para {dd/MM/yyyy HH:mm}.”
    - Fallbacks: se `agendadoPorNome` estiver vazio, usa `nomeMedico`; se `dataHora` estiver nula, exibe “data não informada”.

- Testes
    - Windows:
      ```bash
      cd servicos/servico-notificacao
      ./mvnw.cmd test
      ```
    - Linux/macOS:
      ```bash
      cd servicos/servico-notificacao
      ./mvnw test
      ```

## Segurança (JWT)

Todos os serviços web (exceto rotas abertas como Swagger) exigem JWT Bearer.  
No agendamento, o filtro valida `role` e mapeia permissões: [`SecurityConfig`](file:///e:/Fase3/medconnect-postech/servicos/servico-agendamento/src/main/java/com/adjt/medconnect/servicoagendamento/config/SecurityConfig.java).

## Swagger / API Docs

- Agendamento: `/swagger-ui.html` em http://localhost:8081
- Histórico: `/swagger-ui.html` em http://localhost:8083
- Autenticação: `/swagger-ui.html` em http://localhost:8080 (se habilitado)

## Desenvolvimento local (sem Docker)

Requer Java 21 e Maven instalados. Ajuste `application.yml` de cada serviço para apontar para seu banco local.

Build de um serviço:

```bash
cd servicos/servico-agendamento
mvn package
java -jar target/servico-agendamento-0.0.1-SNAPSHOT.jar
```

## Troubleshooting

- 400 na criação de consulta: verifique se `idPaciente` e `idMedico` existem no serviço de agendamento.
- Token inválido/expirado: gere novamente em `POST /auth/login` (serviço de autenticação).
- Kafka indisponível: a criação de consulta não falha, mas notificações podem não ser enviadas.
- Erro de build no agendamento após atualizar evento: ajuste o construtor de `ConsultaEvent` para incluir `agendadoPorRole` e `agendadoPorNome` (conforme [`ConsultaService.criar`](file:///e:/Fase3/medconnect-postech/servicos/servico-agendamento/src/main/java/com/adjt/medconnect/servicoagendamento/service/ConsultaService.java#L65-L73)).