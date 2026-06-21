# 🚀 SaaS Marketing Platform

Plateforme SaaS de gestion du marketing digital qui aide les entreprises à mieux gérer leur marketing en rassemblant en un seul endroit la gestion des contacts, les campagnes (emails, réseaux sociaux, SMS), l'analyse des résultats et l'automatisation des tâches.

## 📋 Table des matières

- [Technologies](#technologies)
- [Prérequis](#prérequis)
- [Installation](#installation)
- [Configuration](#configuration)
- [Démarrage](#démarrage)
- [Documentation API](#documentation-api)
- [Structure du projet](#structure-du-projet)
- [Profils Spring](#profils-spring)

## 🛠 Technologies

- **Java 21**
- **Spring Boot 3.5.9**
- **Spring Data JPA**
- **PostgreSQL**
- **Spring Modulith**
- **Swagger/OpenAPI (SpringDoc 2.7.0)**
- **Lombok**
- **Maven**

## 📦 Prérequis

- Java 21 ou supérieur
- PostgreSQL 12 ou supérieur
- Maven 3.6+ (ou utiliser le wrapper Maven inclus `./mvnw`)

## ⚙️ Installation

### 1. Cloner le projet

```bash
git clone <url-du-repo>
cd saas
```

### 2. Démarrer l'infrastructure locale (Docker)

Il est fortement recommandé d'utiliser Docker pour lancer PostgreSQL, Redis et MailHog localement.

```bash
# Copier le fichier d'environnement
cp .env.example .env

# Démarrer les services en arrière-plan
docker compose --profile dev up -d

# Vérifier que tout tourne
docker compose ps
```

<details>
<summary>Alternative : Installer PostgreSQL manuellement</summary>

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install postgresql postgresql-contrib

# Démarrer PostgreSQL
sudo systemctl start postgresql
sudo systemctl enable postgresql
```
</details>

### 3. Créer la base de données

```bash
# Se connecter à PostgreSQL
psql -U postgres

# Créer la base de données
CREATE DATABASE saas_db;

# Quitter
\q
```

**OU** utiliser le script SQL fourni :

```bash
psql -U postgres -f create_database.sql
```

### 4. Installer les dépendances Maven

```bash
./mvnw clean install
```

## 🔧 Configuration

### Configuration PostgreSQL

Modifier le fichier `src/main/resources/application.properties` si nécessaire :

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/saas_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### Configuration des profils

Le projet supporte plusieurs profils Spring :

- **default** : Configuration de base
- **dev** : Profil de développement (logs détaillés, base de données de test)
- **prod** : Profil de production (logs minimaux, sécurité renforcée)

Pour activer un profil :

```bash
# Via Maven
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Via variable d'environnement
export SPRING_PROFILES_ACTIVE=dev
./mvnw spring-boot:run

# Via argument JVM
java -jar target/saas-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

## 🚀 Démarrage

### Démarrer l'application

```bash
# Via Maven wrapper
./mvnw spring-boot:run

# Ou avec un profil spécifique
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Vérifier que l'application fonctionne

Une fois démarrée, l'application est accessible sur http://localhost:8080

**Endpoints de test :**

- Page d'accueil : http://localhost:8080/api/
- Health check : http://localhost:8080/api/health
- Swagger UI : http://localhost:8080/swagger-ui.html
- API Docs : http://localhost:8080/api-docs

## 📚 Documentation API

### Swagger UI

L'interface Swagger UI est disponible à l'adresse :

**http://localhost:8080/swagger-ui.html**

Elle fournit une documentation interactive de tous les endpoints de l'API avec la possibilité de tester directement les requêtes.

### API Documentation (OpenAPI JSON)

La spécification OpenAPI complète est disponible à :

**http://localhost:8080/api-docs**

### Endpoints disponibles

#### Health & Info
- `GET /api/` - Page d'accueil de l'API
- `GET /api/health` - Health check

#### Contacts
- `GET /api/contacts` - Récupérer tous les contacts
- `GET /api/contacts/{id}` - Récupérer un contact par ID
- `POST /api/contacts` - Créer un nouveau contact
- `PUT /api/contacts/{id}` - Mettre à jour un contact
- `DELETE /api/contacts/{id}` - Supprimer un contact

### Exemple de requête avec curl

```bash
# Récupérer tous les contacts
curl http://localhost:8080/api/contacts

# Créer un nouveau contact
curl -X POST http://localhost:8080/api/contacts \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@example.com",
    "telephone": "+33612345678",
    "entreprise": "Tech Corp",
    "poste": "Directeur Marketing"
  }'
```

## 📁 Structure du projet

```
saas/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── tg/univlome/saas/
│   │   │       ├── SaasApplication.java
│   │   │       ├── config/
│   │   │       │   ├── OpenApiConfig.java
│   │   │       │   └── HealthController.java
│   │   │       └── marketing/
│   │   │           ├── analytique/
│   │   │           ├── authentification/
│   │   │           ├── campagne/
│   │   │           ├── contact/
│   │   │           │   ├── application/
│   │   │           │   │   ├── controllers/
│   │   │           │   │   ├── dtos/
│   │   │           │   │   └── mappers/
│   │   │           │   ├── domain/
│   │   │           │   │   ├── enums/
│   │   │           │   │   ├── models/
│   │   │           │   │   └── services/
│   │   │           │   └── repositories/
│   │   │           ├── email/
│   │   │           ├── reseaux_sociaux/
│   │   │           └── shared/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       └── application-prod.properties
│   └── test/
├── pom.xml
├── README.md
├── CONFIG_README.md
├── SUMMARY.md
├── NEXT_STEPS.md
└── create_database.sql
```

## 📖 Modules

Le projet est organisé en modules suivant l'architecture Spring Modulith :

- **analytique** : Analyse des données et statistiques
- **authentification** : Gestion de l'authentification et des utilisateurs
- **campagne** : Gestion des campagnes marketing
- **contact** : Gestion des contacts
- **email** : Gestion des emails
- **reseaux_sociaux** : Intégration des réseaux sociaux
- **shared** : Composants partagés

## 🔨 Commandes utiles

### Maven

```bash
# Nettoyer et compiler
./mvnw clean compile

# Exécuter les tests
./mvnw test

# Construire le projet (sans tests)
./mvnw clean install -DskipTests

# Démarrer l'application
./mvnw spring-boot:run

# Construire un JAR exécutable
./mvnw clean package
```

### PostgreSQL

```bash
# Se connecter à PostgreSQL
psql -U postgres

# Lister les bases de données
\l

# Se connecter à la base saas_db
\c saas_db

# Lister les tables
\dt

# Quitter psql
\q
```

## 📝 Documentation complémentaire

- **CONFIG_README.md** : Guide détaillé de configuration PostgreSQL et Swagger
- **SUMMARY.md** : Résumé de la configuration effectuée
- **NEXT_STEPS.md** : Prochaines étapes après l'installation
- **create_database.sql** : Script SQL de création de la base de données

## 🤝 Contribution

1. Fork le projet
2. Créer une branche (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

## 📄 Licence

Ce projet est développé dans le cadre d'un projet universitaire à l'Université de Lomé (Togo).

## 📧 Contact

Pour toute question ou suggestion, contactez l'équipe de développement.

---

Développé avec ❤️ par l'équipe SaaS Marketing Platform

