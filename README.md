# OdontoUFRGS — Backend (Spring Boot)

API REST do sistema de gerenciamento de atividades clínicas da Faculdade de Odontologia da UFRGS.
Stack: **Java 21 · Spring Boot 3.4 · PostgreSQL 16 · Maven**.

> **Documentação completa** (arquitetura, DER, fluxos, infra, backlog) em
> **[NatanPivetta/odonto-docs](https://github.com/NatanPivetta/odonto-docs)**.

## Pré-requisitos

| Ferramenta       | Versão   | Observação                            |
|------------------|----------|---------------------------------------|
| JDK              | 21       | Exigido pelo `pom.xml`.               |
| Docker + Compose | recente  | Caminho recomendado para o PostgreSQL. |
| PostgreSQL       | 16       | Só se optar por não usar Docker.      |
| Maven            | —        | Use o wrapper `./mvnw` (incluído).    |

## Quick start

```bash
# 1. Variáveis de ambiente
cp .env.example .env
# Edite .env: defina MAIL_USERNAME, MAIL_PASSWORD e um JWT_SECRET forte.
# Para desenvolvimento, os defaults do .env.example já permitem subir.

# 2. Banco via Docker
docker compose up -d postgres

# 3. Perfil local
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml

# 4. API
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

- API: `http://localhost:8090/api`
- Swagger UI: `http://localhost:8090/api/swagger-ui.html`
- Tabelas criadas automaticamente pelo Liquibase no startup.

**Usuário admin inicial:** cartão `000000001`, senha `Admin@123` — troque após o primeiro login.

## Subir tudo via Docker Compose (banco + API)

```bash
cp .env.example .env
docker compose up -d --build
# opcional: docker compose --profile tools up -d pgadmin  →  http://localhost:5050
```

## Testes

```bash
./mvnw test     # requer Docker (Testcontainers sobe um PostgreSQL real)
```

## Variáveis de ambiente

Arquivo de referência: `.env.example`.

| Variável                          | Descrição                                                            |
|-----------------------------------|----------------------------------------------------------------------|
| `SPRING_DATASOURCE_URL`           | JDBC URL (`localhost` direto; `postgres` no Docker Compose).         |
| `SPRING_DATASOURCE_USERNAME`      | Usuário do banco.                                                    |
| `SPRING_DATASOURCE_PASSWORD`      | Senha do banco.                                                      |
| `POSTGRES_DB/USER/PASSWORD`       | Usados pelo Compose para criar o container do PostgreSQL.            |
| `JWT_SECRET`                      | Segredo HS512 (≥ 64 bytes). **Obrigatório em qualquer ambiente real.** Gere com `openssl rand -base64 64`. |
| `JWT_EXPIRATION_MS`               | Validade do token em ms (padrão: `86400000` = 24h).                 |
| `CORS_ALLOWED_ORIGINS`            | Origens do frontend autorizadas (CSV).                               |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Credenciais SMTP (Gmail "senha de app") — obrigatório para o fluxo de primeiro acesso. |
| `SPRING_PROFILES_ACTIVE`          | Perfil ativo. Disponível: `local`. Deixe vazio para o perfil base.  |

## Documentação completa

Acesse **[NatanPivetta/odonto-docs](https://github.com/NatanPivetta/odonto-docs)** para:

- Arquitetura, DER e casos de uso
- Convenções de código e estrutura de pacotes
- Setup detalhado do frontend
- Infraestrutura AWS (Terraform) e CI/CD
- Bugs conhecidos e backlog