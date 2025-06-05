# Etapa de compilación
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY . .

# Ejecutar tests y generar reporte de Jacoco
RUN mvn clean verify -B

# Etapa de ejecución
FROM eclipse-temurin:17-jre

WORKDIR /app

# Descargar OpenTelemetry Java Agent para auto-instrumentación
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

# Copiamos el artefacto final
COPY --from=build /app/target/*.jar application.jar

# --- ARG con la versión que inyectará el workflow ---
ARG APP_VERSION
ENV APP_VERSION=${APP_VERSION}

# Variables de entorno para OpenTelemetry Agent
ENV OTEL_SERVICE_NAME=k8_auth
ENV OTEL_RESOURCE_ATTRIBUTES=service.name=k8_auth,environment=dev
ENV OTEL_INSTRUMENTATION_SPRING_WEB_ENABLED=true
ENV OTEL_INSTRUMENTATION_SPRING_WEBMVC_ENABLED=true
ENV OTEL_INSTRUMENTATION_RESTTEMPLATE_ENABLED=true
ENV OTEL_INSTRUMENTATION_APACHE_HTTPCLIENT_ENABLED=true
ENV OTEL_PROPAGATORS=tracecontext,baggage,b3multi
ENV OTEL_LOGS_EXPORTER=none
ENV OTEL_EXPORTER_OTLP_TRACES_ENDPOINT=http://otel-collector-opentelemetry-collector.monitoring.svc.cluster.local:4318/v1/traces

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
RUN chown appuser:appgroup /app/opentelemetry-javaagent.jar
USER appuser

EXPOSE 8080

# Ejecutar con el agente OpenTelemetry para auto-instrumentación
ENTRYPOINT ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "-jar", "application.jar"]

#docker rm -f k8_payment_container && docker build -t k8_payment . && docker run -p 8080:8080 --name k8_payment_container k8_payment