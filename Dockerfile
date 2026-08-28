# ==========================================
# Étape 1 : Build de l'application (Maven)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copier le fichier pom.xml et télécharger les dépendances (cache Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier les sources et compiler l'application
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Étape 2 : Image d'exécution (JRE 21)
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -S saasgroup && adduser -S saasuser -G saasgroup
USER saasuser:saasgroup

# Copier l'artefact JAR compilé
COPY --from=builder /app/target/saas-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=dev", "app.jar"]
