# 🛒 API de Gerenciamento de Vendas com Cursor Pagination Avançada

API REST Spring Boot para gerenciamento de **vendas** (orders, products, offices/filiais, order_products como relação).  
Implementa **cursor-based pagination** (keyset) em entidades com **chave primária simples** e **chave composta**, cache Redis por entidade, rate limiting com Resilience4j, autenticação JWT stateless e containerização completa.

O foco é **escalabilidade** em listas grandes (ex: itens de pedidos), evitando os problemas de performance do offset/limit tradicional.

---

## 📋 Índice

- [Sobre o projeto](#-sobre-o-projeto)
- [Por que cursor pagination?](#-por-que-cursor-pagination)
- [Tecnologias](#-tecnologias)
- [Funcionalidades principais](#-funcionalidades-principais)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação e Execução](#-instalação-e-execução)
- [Endpoints da API](#-endpoints-da-api)
- [Exemplos de Requisições](#-exemplos-de-requisições)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Decisões & Aprendizados](#-decisões--aprendizados)
- [Documentação Swagger](#-documentação-swagger)
- [Licença](#-licença)

---

## 🚀 Sobre o projeto

Sistema backend para gerenciar **vendas** integrando clientes, construtores de produtos, filiais (offices) e itens de pedidos (order_product).  
Destaque para paginação eficiente via **cursor** (keyset pagination) em tabelas com chave composta (ex: `orderId + productId`), cache L2 com Redis e proteção contra abuso.

Perfeito para demonstrar arquitetura escalável em portfólio ou entrevistas técnicas.

---

## ⚡ Por que cursor pagination?

| Abordagem          | Performance em escala | Estabilidade (duplicatas/pulos) | Suporte a chave composta | Complexidade |
|--------------------|-----------------------|----------------------------------|---------------------------|--------------|
| Offset + Limit     | Degrada (full scan)   | Pode pular/duplicar              | Simples                   | Baixa        |
| Cursor (keyset)    | Constante (index seek)| Estável com ordenação única      | Sim (com predicados aninhados) | Média-Alta   |

Implementado com **predicados aninhados** para chaves compostas (ex: `(orderId > x) OR (orderId = x AND productId > y)`).

---

## 🛠 Tecnologias

| Tecnologia          | Versão     | Finalidade principal                              |
|---------------------|------------|---------------------------------------------------|
| Java                | 17+        | Linguagem                                         |
| Spring Boot         | 3.x        | Framework principal                               |
| Spring Security     | 6.x        | JWT stateless + refresh                           |
| Spring Data JPA     | 3.x        | Persistência + queries customizadas para keyset   |
| PostgreSQL          | 15+        | Banco principal                                   |
| Redis               | —          | Cache por entidade (Offices, OrderProducts, etc.) |
| Resilience4j        | —          | Rate limiting granular                            |
| OpenAPI / Swagger   | 2.x        | Documentação interativa com bearer JWT            |
| Docker + Compose    | 24+ / 2.x  | Containerização (app + pg + redis)                |

---

## ✨ Funcionalidades principais

- CRUD completo para entidades com relacionamentos complexos.
- Paginação por cursor com suporte a **chave composta** (ex: OrderProduct)
- Cache de leitura individual com Redis (write-behind)
- Rate limiting separado (leitura pública, item único, escrita)
- Autenticação JWT (access + refresh) + UserDetails custom
- Tratamento global de exceções (Problem Details RFC 7807)
- Cursor codificado em Base64(JSON) para evolução futura
- Swagger com esquema bearer JWT
---

## 📦 Pré-requisitos

- Java 17+
- Maven 3.9+
- Docker + Docker Compose

---

## 🚀 Instalação e Execução

### Com Docker Compose (recomendado)

```bash
git clone https://github.com/costtinha/cursor_pagination.git
cd cursor_pagination
docker compose up -d --build

## 🚀 Instalação e Execução

# A API estará disponível em http://localhost:8080
```

### Sem Docker

```bash
# Clone o repositório
git clone https://github.com/costtinha/cursor_pagination.git
cd cursor_pagination

# Configure o banco de dados PostgreSQL local
# (ajuste as credenciais em src/main/resources/application.yml)

# Build e execução
./mvnw spring-boot:run
```

---

## 🔗 Endpoints da API

### Públicos (sem autenticação)

| Método            | Endpoint          | Descrição          | Cursor? |
|-------------------|-------------------|--------------------|---------|
| `GET`             | `/offices`        | Lista offset-based | Não     |
| `GET`             | `/offices/keyset` | Lista cursor-based | Sim     |
| `GET`             | `/offices/{id}`   | Com cache          | Não     |
| `POST/PUT/DELETE` | `/offices/...`    | CRUD               | Não     |

| Método | Endpoint                               | Descrição          | Cursor |
|--------|----------------------------------------|--------------------|--------|
| `GET`  | `/order_product`                       | Lista offset-based | Não    |
| `GET`  | `/order_product/keyset`                | Lista cursor-based | Não    |
| `GET`  | `/order_product/{orderId}/{productId}` | Com cache          | Não    |
| `GET`   | `/order_product/....`                   | CRUD               | Não    |


---

## 📝 Exemplos de Requisições

### Registrar usuário

```bash
curl "http://localhost:8080/public/api/order_product/keyset?size=10&direction=NEXT"
```
```bash
curl "http://localhost:8080/public/api/order_product/keyset?cursor=eyJ...&size=10&direction=NEXT"
```


**Resposta (201 Created):**
```json
{
  "content": [ ... array de OrderProductDto ... ],
  "nextCursor": "eyJ...base64...",
  "prevCursor": "eyJ...base64...",
  "hasNext": true,
  "hasPrev": true
}
```
### Criar OrderProduct

```bash
 curl -X POST http://localhost:8080/public/api/order_product \
  -H "Authorization: Bearer SEU_JWT_AQUI" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1001,
    "productId": 5,
    "qnty": 3,
    "priceEach": 149.90
  }'
```


---

## 📁 Estrutura do Projeto

```
src/main/java/com.tcc/
├── cache/                # Repositórios Redis (ex: OfficeCacheRepository, OrderProductCacheRepository)
├── components/           # CursorCodec (encode/decode Base64+JSON)
├── config/               # SecurityConfig, JwtFilter, OpenApiConfig, RepositoryConfig
├── controller/           # OfficeController, OrderProductController, RateLimitedController (base)
├── dtos/                 # OfficeDto, OrderProductDto, CursorPageResponse
│   └── cursors/          # OfficeCursor, OrderProductCursor
├── entity/               # Office, OrderProduct, OrderProductKey (composite), User, etc.
├── exception/            # ResourceNotFoundException, ConflictException, GlobalExceptionHandler
├── pagination/           # PageDirection (NEXT/PREV enum)
├── persistance/          # OfficeRepository, OrderProductRepository (JPA + @Query keyset)
├── service/
│   ├── authService/      # JwtService, CustomUserDetailsService
│   ├── officeService/    # OfficeService (CRUD + cursor simples)
│   └── orderProductService/ # OrderProductService (CRUD + cursor composto)
└── CodeApplication.java  # Classe principal
```


---

## 📚 Decisões e Aprendizados

Este projeto foi desenvolvido como estudo prático dos seguintes conceitos:

Cursor simples (ex: officeId) vs composto (ex: orderId + productId)
Predicados JPA aninhados para chaves compostas → (orderId > x) OR (orderId = x AND productId > y)
Cache Redis por entidade com chave custom (ex: "orderId: 1001,productId: 5")
Rate limiting granular (público/leitura/escrita)
Cursor em Base64(JSON) → permite adicionar mais campos no futuro (ex: timestamp)
Ordenação estável e indexada obrigatória para performance
Problem Details padronizado para erros

---

## 📄 Licença

Este projeto é de uso educacional e está disponível sob a licença [MIT](LICENSE).

---

<p align="center">
  Desenvolvido por <a href="https://github.com/costtinha">Daniel Costa</a>
</p>
