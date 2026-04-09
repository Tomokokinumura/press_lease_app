FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw mvnw
COPY pom.xml pom.xml

RUN chmod +x ./mvnw
RUN ./mvnw -B dependency:go-offline

COPY src/ src/

RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 10000

ENTRYPOINT ["java", "-Dserver.port=10000", "-jar", "/app/app.jar"]
