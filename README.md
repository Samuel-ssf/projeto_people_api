# People API

> Uma API REST para gestão de pessoas, com autenticação, persistência PostgreSQL, validações e estimativa de nacionalidade.

![Java 17](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![Authentication](https://img.shields.io/badge/Auth-Basic%20Authentication-2f81f7)

## Visão geral

O projeto foi desenvolvido como desafio técnico Full Stack. Ele disponibiliza uma API protegida para criar, consultar e excluir pessoas, além de uma interface web responsiva para consumir os recursos.

```text
Interface web ──► API REST ──► Service ──► PostgreSQL
                    │
                    └────────► Nationalize.io
```

## Principais recursos

- Cadastro, listagem, busca e exclusão de pessoas.
- Validações amigáveis no front-end e no back-end.
- Basic Authentication em todas as APIs.
- Consulta de nacionalidade provável pela Nationalize.
- Interface dark SaaS, responsiva, com login, toasts, modais e estados de carregamento.
- Tratamento consistente de erros HTTP.
- Testes automatizados com banco H2 em memória.

## Tecnologias

| Camada | Tecnologia |
| --- | --- |
| Backend | Java 17, Spring Boot 4.1.1, Spring Web MVC |
| Dados | Spring Data JPA, Hibernate, PostgreSQL 16 |
| Segurança | Spring Security + HTTP Basic |
| Validação | Jakarta Bean Validation |
| Front-end | HTML, CSS e JavaScript puros |
| Testes | JUnit, MockMvc, H2 |
| Infraestrutura local | Docker Compose |

## Estrutura do projeto

```text
src/main/java/com/samuel/people_api
├── config/        # Segurança e configurações externas
├── controller/    # Endpoints REST
├── dto/           # Contratos de request e response
├── exception/     # Tratamento centralizado de erros
├── integration/   # Cliente da Nationalize
├── model/         # Entidade JPA Person
├── repository/    # Repositório JPA
└── service/       # Regras de negócio

src/main/resources/static
├── index.html     # Interface web
├── css/styles.css # Design system visual
└── js/app.js      # Integração com a API
```

## Persistência

Não foram criadas entidades extras porque o desafio gerencia somente pessoas. A estrutura atual é simples e suficiente:

| Classe | Responsabilidade |
| --- | --- |
| `model/Person` | Entidade JPA mapeada para a tabela `person` |
| `repository/PersonRepository` | Consultas e persistência usando `JpaRepository` |
| `service/PersonService` | Regras de cadastro, busca, exclusão e nacionalidade |

### Tabela `person`

| Coluna | Tipo | Regra |
| --- | --- | --- |
| `id` | `BIGINT` | Gerado automaticamente |
| `document` | `VARCHAR(20)` | Único e obrigatório |
| `name` | `VARCHAR(100)` | Obrigatório |
| `last_name` | `VARCHAR(100)` | Obrigatório |
| `email` | `VARCHAR(150)` | Único e obrigatório |

O Hibernate cria ou atualiza a tabela automaticamente ao iniciar a aplicação.

## Como executar

### Pré-requisitos

- Java 17 ou superior
- Docker Desktop em execução

### 1. Suba o PostgreSQL

```powershell
docker compose up -d
```

### 2. Configure o Java e inicie a API

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
.\mvnw.cmd spring-boot:run
```

Quando aparecer `Tomcat started on port 8080`, a aplicação estará disponível.

### 3. Acesse a interface

Abra [http://localhost:8080](http://localhost:8080).

| Campo | Valor local |
| --- | --- |
| Usuário | `apiuser` |
| Senha | `change-me` |

As credenciais podem ser substituídas pelas variáveis de ambiente `APP_SECURITY_USERNAME` e `APP_SECURITY_PASSWORD`.

## Endpoints

Todas as APIs exigem HTTP Basic Authentication.

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `POST` | `/registrarName` | Cadastra uma pessoa |
| `GET` | `/list` | Lista todas as pessoas |
| `GET` | `/list/{id}` | Busca uma pessoa por ID |
| `DELETE` | `/list/{id}` | Remove uma pessoa por ID |
| `GET` | `/findNacionalityByPerson/{id}` | Consulta uma nacionalidade provável |

### Exemplo de cadastro

```bash
curl -u apiuser:change-me -X POST http://localhost:8080/registrarName \
  -H "Content-Type: application/json" \
  -d '{"document":"123456789","name":"Nathaniel","lastName":"Silva","email":"nathaniel@example.com"}'
```

```json
{
  "id": 1,
  "document": "123456789",
  "name": "Nathaniel",
  "lastName": "Silva",
  "email": "nathaniel@example.com"
}
```

### Exemplo de nacionalidade

```bash
curl -u apiuser:change-me http://localhost:8080/findNacionalityByPerson/1
```

```json
{
  "personId": 1,
  "personName": "Nathaniel",
  "countryCode": "US",
  "nationality": "Estados Unidos"
}
```

> A nacionalidade é uma estimativa fornecida pela Nationalize a partir do primeiro nome. Não representa uma confirmação de nacionalidade.

## Validações e respostas de erro

| Campo | Regra |
| --- | --- |
| Documento | Obrigatório; 7 a 12 dígitos numéricos, sem checagem de autenticidade ou dígito verificador |
| Nome e sobrenome | Obrigatórios; até 100 caracteres |
| E-mail | Obrigatório, formato de e-mail válido; até 150 caracteres |
| ID | Número positivo |

Exemplo de resposta para pessoa não encontrada:

```json
{
  "timestamp": "2026-09-05T12:00:00Z",
  "status": 404,
  "message": "Pessoa com id 99 não encontrada",
  "errors": {}
}
```

## Testes

Os testes usam H2 em memória e não exigem que o PostgreSQL esteja ativo.

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
.\mvnw.cmd test
```

A suíte cobre autenticação, cadastro, busca, exclusão, validação do documento, dados inválidos e ID inválido.

## Encerrar o ambiente

```powershell
docker compose down
```
