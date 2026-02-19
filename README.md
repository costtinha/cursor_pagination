# 🔐 Spring Security - Autenticação e Autorização

API REST com implementação de autenticação e autorização utilizando **Spring Security**, **JWT** e **Spring Boot**. O projeto demonstra boas práticas de segurança em aplicações Java, incluindo controle de acesso baseado em roles, proteção de endpoints e containerização com Docker.

---

## 📋 Índice

- [Tecnologias](#-tecnologias)
- [Arquitetura](#-arquitetura)
- [Funcionalidades](#-funcionalidades)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação e Execução](#-instalação-e-execução)
- [Endpoints da API](#-endpoints-da-api)
- [Exemplos de Requisições](#-exemplos-de-requisições)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Aprendizados](#-aprendizados)
- [Licença](#-licença)

---

## 🛠 Tecnologias

| Tecnologia | Versão | Descrição |
|---|---|---|
| Java | 17+ | Linguagem principal |
| Spring Boot | 3.x | Framework base |
| Spring Security | 6.x | Autenticação e autorização |
| Spring Data JPA | 3.x | Persistência de dados |
| Maven | 3.9+ | Gerenciamento de dependências |
| Docker | 24+ | Containerização |
| Docker Compose | 2.x | Orquestração de containers |
| PostgreSQL | 15+ | Banco de dados relacional |

---

## 🏗 Arquitetura

```
┌─────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   Cliente    │────▸│  Security Filter │────▸│   Controller    │
│  (Postman)   │     │     Chain        │     │                 │
└─────────────┘     └──────────────────┘     └────────┬────────┘
                           │                          │
                    ┌──────┴──────┐            ┌──────┴──────┐
                    │    JWT      │            │   Service   │
                    │  Validation │            │    Layer    │
                    └─────────────┘            └──────┬──────┘
                                                      │
                                               ┌──────┴──────┐
                                               │ Repository  │
                                               │   (JPA)     │
                                               └──────┬──────┘
                                                      │
                                               ┌──────┴──────┐
                                               │ PostgreSQL  │
                                               └─────────────┘
```

---

## ✨ Funcionalidades

- **Registro de usuários** com senha criptografada (BCrypt)
- **Login com geração de token JWT**
- **Autorização baseada em roles** (ADMIN, USER)
- **Proteção de endpoints** por nível de acesso
- **Filtro de autenticação** customizado na Security Filter Chain
- **Containerização** completa com Docker e Docker Compose

---

## 📦 Pré-requisitos

Certifique-se de ter instalado:

- **Java 17+** → [Download](https://adoptium.net/)
- **Maven 3.9+** → [Download](https://maven.apache.org/download.cgi)
- **Docker e Docker Compose** → [Download](https://www.docker.com/products/docker-desktop/)

---

## 🚀 Instalação e Execução

### Com Docker (recomendado)

```bash
# Clone o repositório
git clone https://github.com/costtinha/estudos_spring_security.git
cd estudos_spring_security

# Suba os containers (aplicação + banco de dados)
docker-compose up -d

# A API estará disponível em http://localhost:8080
```

### Sem Docker

```bash
# Clone o repositório
git clone https://github.com/costtinha/estudos_spring_security.git
cd estudos_spring_security

# Configure o banco de dados PostgreSQL local
# (ajuste as credenciais em src/main/resources/application.properties)

# Build e execução
./mvnw spring-boot:run
```

---

## 🔗 Endpoints da API

### Públicos (sem autenticação)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/auth/register` | Registrar novo usuário |
| `POST` | `/auth/login` | Autenticar e obter token JWT |

### Protegidos (requer token JWT)

| Método | Endpoint | Role | Descrição |
|---|---|---|---|
| `GET` | `/users` | `ADMIN` | Listar todos os usuários |
| `GET` | `/users/{id}` | `USER`, `ADMIN` | Buscar usuário por ID |

> ⚠️ **Nota:** Adapte a tabela acima conforme os endpoints reais da sua aplicação.

---

## 📝 Exemplos de Requisições

### Registrar usuário

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "daniel",
    "password": "senha123",
    "role": "USER"
  }'
```

**Resposta (201 Created):**
```json
{
  "id": 1,
  "username": "daniel",
  "role": "USER"
}
```

### Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "daniel",
    "password": "senha123"
  }'
```

**Resposta (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Acessar endpoint protegido

```bash
curl -X GET http://localhost:8080/users \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 📁 Estrutura do Projeto

```
src/main/java/com/costtinha/security/
├── config/
│   └── SecurityConfig.java          # Configuração do Spring Security
├── controller/
│   ├── AuthController.java          # Endpoints de autenticação
│   └── UserController.java          # Endpoints de usuários
├── dto/
│   ├── LoginRequest.java            # DTO de login
│   └── RegisterRequest.java         # DTO de registro
├── entity/
│   ├── User.java                    # Entidade usuário
│   └── Role.java                    # Enum de roles
├── filter/
│   └── JwtAuthenticationFilter.java # Filtro JWT na filter chain
├── repository/
│   └── UserRepository.java          # Repositório JPA
├── service/
│   ├── AuthService.java             # Lógica de autenticação
│   ├── JwtService.java              # Geração/validação de tokens
│   └── UserService.java             # Lógica de negócio
└── SecurityApplication.java         # Classe principal
```

> ⚠️ **Nota:** Ajuste os nomes dos pacotes e classes conforme a estrutura real do seu projeto.

---

## 📚 Aprendizados

Este projeto foi desenvolvido como estudo prático dos seguintes conceitos:

- **Security Filter Chain** — como o Spring Security intercepta e processa requisições HTTP
- **Autenticação stateless com JWT** — geração, assinatura e validação de tokens
- **BCrypt** — hashing seguro de senhas com salt automático
- **Role-Based Access Control (RBAC)** — controle de acesso granular por perfil de usuário
- **SecurityContext** — como o Spring mantém informações do usuário autenticado durante a requisição
- **Docker multi-stage build** — containerização eficiente de aplicações Java

---

## 📄 Licença

Este projeto é de uso educacional e está disponível sob a licença [MIT](LICENSE).

---

<p align="center">
  Desenvolvido por <a href="https://github.com/costtinha">Daniel Costa</a>
</p>
