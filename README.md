# Pollen

API REST para acompanhamento de projetos, tarefas e equipes. O backend organiza usuários, colmeias (teams), projetos, tarefas e comentários em um monólito modular.

## Tecnologias

- Java 21
- Spring Boot 3.3.4
- Spring Data JPA e Hibernate
- Spring Modulith 1.2.3
- MySQL 8.4
- MapStruct 1.5.5
- Lombok 1.18.34
- Springdoc OpenAPI
- Maven

## Arquitetura

O projeto usa uma arquitetura modular com Spring Modulith. Os módulos principais são:

- `usuario`: usuários e hierarquia de perfis
- `team`: colmeias, gestores e colaboradores
- `projeto`: projetos, responsáveis, progresso e prazos
- `tarefa`: tarefas, subtarefas, status e andamento
- `comentario`: comentários associados a projetos
- `admin`: dashboard e operações administrativas
- `shared`: enums e objetos de valor compartilhados

As classes dentro de `internal/` são privadas ao módulo. A comunicação entre módulos deve ocorrer por serviços públicos ou eventos de domínio, como `TarefaConcluidaEvent`.

## Requisitos

- JDK 21
- Maven 3.9 ou superior
- MySQL 8.4 ou Docker com Docker Compose

## Executando localmente

1. Configure um banco MySQL chamado `pollen`.
2. Defina as variáveis de conexão, se necessário:

```bash
export SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3306/pollen?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true'
export SPRING_DATASOURCE_USERNAME='seu_usuario'
export SPRING_DATASOURCE_PASSWORD='sua_senha'
```

3. Compile e execute:

```bash
./mvnw spring-boot:run
```

Caso o Maven Wrapper não esteja disponível, use:

```bash
mvn spring-boot:run
```

A API será iniciada em `http://localhost:8080`.

## Executando com Docker

O ambiente Docker inicia o MySQL na porta `3307` e a aplicação na porta `8081`:

```bash
docker compose up --build
```

A API ficará disponível em `http://localhost:8081`.

Para encerrar os serviços:

```bash
docker compose down
```

## Documentação da API

Com a aplicação em execução, acesse:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Ao usar Docker, substitua a porta `8080` por `8081`.

## Endpoints principais

### Usuários

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/usuarios` | Cadastrar usuário |
| GET | `/usuarios` | Listar usuários |
| GET | `/usuarios/{id}` | Consultar usuário |
| PUT | `/usuarios/{id}` | Atualizar usuário |
| DELETE | `/usuarios/{id}` | Excluir usuário |

### Teams

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/teams` | Criar colmeia |
| GET | `/teams` | Listar colmeias |
| GET | `/teams/{id}` | Consultar colmeia |
| PUT | `/teams/{id}` | Atualizar colmeia |
| DELETE | `/teams/{id}` | Excluir colmeia |
| POST | `/teams/{id}/colaboradores` | Adicionar colaborador |
| DELETE | `/teams/{id}/colaboradores/{uid}` | Remover colaborador |

### Projetos

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/teams/{id}/projetos` | Criar projeto |
| GET | `/teams/{id}/projetos` | Listar projetos da colmeia |
| GET | `/projetos/{id}` | Consultar projeto |
| PUT | `/projetos/{id}` | Atualizar projeto |
| DELETE | `/projetos/{id}` | Excluir projeto |
| POST | `/projetos/{id}/responsaveis` | Associar responsáveis |

### Tarefas e comentários

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/projetos/{id}/tarefas` | Criar tarefa |
| GET | `/projetos/{id}/tarefas` | Listar tarefas |
| GET | `/tarefas/{id}` | Consultar tarefa |
| PUT | `/tarefas/{id}` | Atualizar tarefa |
| DELETE | `/tarefas/{id}` | Excluir tarefa |
| PATCH | `/tarefas/{id}/status` | Atualizar status |
| POST | `/tarefas/{id}/subtarefas` | Criar subtarefa |
| POST | `/projetos/{id}/comentarios` | Adicionar comentário |
| GET | `/projetos/{id}/comentarios` | Listar comentários |

### Administração

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/admin/dashboard` | Consultar dashboard administrativo |

## Modelo de domínio

O domínio contém:

- Usuários com herança JPA `JOINED`: `Usuario`, `Administrador`, `Gestor` e `Colaborador`.
- `Team` com gestor responsável e colaboradores.
- `Projeto` associado a um team, responsáveis, tarefas e comentários.
- `Tarefa` com status, prazo, andamento e subtarefas.
- Objetos embutidos `Contato`, `Prazo` e `Andamento`.
- Enums `Classificacao` (`OCULTO`, `PUBLICO`) e `Status` (`PENDENTE`, `PROGREDINDO`, `CONCLUIDO`).

## Testes

Para executar os testes:

```bash
mvn test
```

Os testes cobrem os serviços de usuários, teams, projetos, tarefas e comentários.

## Estado do projeto

O plano de ação registra como próximas evoluções:

- Formalizar o tipo e os valores válidos de `setor`.
- Definir as regras completas de comentários.
- Especificar o fluxo de convite por link.
- Modelar a entidade `Empresa` para o dashboard administrativo.
- Definir controle de acesso por perfil com Spring Security.

## Arquivos úteis

- `src/main/resources/http/`: exemplos de requisições HTTP.
- `plano-de-acao.md`: análise detalhada do domínio, arquitetura e pendências.
- `docker-compose.yml`: ambiente local com MySQL e aplicação.
