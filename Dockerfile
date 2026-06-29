# ─────────────────────────────────────────────────────────────
# Stage 1 – Build
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copia apenas os arquivos de dependência primeiro (melhor cache de camadas)
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copia o restante do código e faz o build
COPY src ./src
RUN ./mvnw package -DskipTests -q

# ─────────────────────────────────────────────────────────────
# Stage 2 – Runtime
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Usuário não-root por segurança
RUN addgroup -S odonto && adduser -S odonto -G odonto
USER odonto

# Copia apenas o jar gerado
COPY --from=builder /app/target/*.jar app.jar

# A aplicação escuta na porta 8090 (ver server.port em application.yml)
EXPOSE 8090

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
