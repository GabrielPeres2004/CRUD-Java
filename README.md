# CRUD-Java

API REST de CRUD de clientes desenvolvida em **Java com Spring Boot**, criada como base para deploy em ambiente DevOps com Docker e MySQL.

## 🛠️ Tecnologias

- Java 21
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Data JPA
- Spring Validation
- H2 Database (testes)
- MySQL (produção)
- Maven

## 📁 Estrutura do Projeto

```
src/main/java/com/gabriel/crudjava
├── config/            → Configurações e seeder de dados
├── controller/        → Endpoints REST e tratamento de exceções
├── domain/            → Entidades JPA
├── dtos/
│   ├── request/       → DTOs de entrada
│   └── response/      → DTOs de saída
├── repositories/      → Repositórios Spring Data JPA
└── services/          → Regras de negócio e exceções
```

## 🌐 Endpoints

Base URL: `http://localhost:8080`

| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/customers` | Lista todos os clientes |
| `GET` | `/customers/{id}` | Busca cliente por ID |
| `POST` | `/customers` | Cria um novo cliente |
| `PUT` | `/customers/{id}` | Atualiza um cliente |
| `DELETE` | `/customers/{id}` | Remove um cliente |

### Exemplo de body (POST / PUT)

```json
{
  "name": "João Silva",
  "email": "joao@email.com",
  "phone": "(11) 99999-1111",
  "address": "Rua das Flores, 123 - São Paulo/SP"
}
```

## ⚙️ Como rodar localmente

### Pré-requisitos

- Java 21+
- Maven

### Passos

```bash
# Clone o repositório
git clone <url-do-repositorio>

# Entre na pasta do projeto
cd CRUD-Java

# Rode a aplicação (perfil de teste com H2)
mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

O console do H2 fica disponível em `http://localhost:8080/h2-console`  
- **JDBC URL:** `jdbc:h2:mem:testdb`  
- **Username:** `sa`  
- **Password:** *(vazio)*

## 🗄️ Banco de Dados — Produção (MySQL)

Para rodar com MySQL, ative o perfil `prod` e configure as variáveis de ambiente:

```bash
SPRING_PROFILES_ACTIVE=prod
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=crudjava
MYSQL_USER=root
MYSQL_PASSWORD=sua_senha
```

Ou rode diretamente:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 🐳 Docker

> Em breve — o Docker será configurado para orquestrar a aplicação com MySQL via `docker-compose`.

## 🔓 CORS

As origens permitidas ficam na propriedade `cors.allowed-origins` (padrão: `localhost:3000`, `4200` e `5173`).
Para alterar, defina a variável de ambiente `CORS_ALLOWED_ORIGINS` com as origens separadas por vírgula.

## 🧪 Testes

```bash
mvn test
```

- `CustomerServiceTest` → testes unitários (Mockito)
- `CustomerControllerTest` → testes de integração (MockMvc + H2)

## 📋 Commits

O projeto segue o padrão **Conventional Commits** em português.
