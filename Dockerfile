# ======================================================
# Multi-stage Dockerfile — Plateforme SaaS Backend
# ======================================================

# ── Étape 1 : Compilation avec Maven ──────────────────
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copier les fichiers Maven en premier (cache des dépendances)
COPY pom.xml mvnw ./
COPY .mvn .mvn

# Télécharger les dépendances hors-ligne (couche cachée)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copier le code source et compiler
COPY src src
RUN ./mvnw package -DskipTests -B

# ── Étape 2 : Image d'exécution allégée (JRE) ────────
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copier le JAR compilé depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar

# Configurer les métadonnées du conteneur
EXPOSE 8080

# Passer en utilisateur non-root
USER appuser

# Lancer l'application avec le profil de production
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
