# Etapa 1: Compilación de la aplicación
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
# Descarga las dependencias en caché para acelerar compilaciones futuras
RUN mvn dependency:go-offline -B
COPY src ./src
# Compila generando el jar en formato fast-jar (por defecto en Quarkus)
RUN mvn package -DskipTests

# Etapa 2: Imagen de ejecución ligera
FROM eclipse-temurin:21-jre-alpine
WORKDIR /deployments
# Copia las librerías y dependencias generadas por Quarkus
COPY --from=build /app/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build /app/target/quarkus-app/*.jar /deployments/
COPY --from=build /app/target/quarkus-app/app/ /deployments/app/
COPY --from=build /app/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar quarkus-run.jar" ]