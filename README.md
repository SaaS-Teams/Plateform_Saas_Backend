# 🚀 SaaS Marketing Platform — Core Backend & Microservices

Plateforme SaaS de gestion du marketing digital de nouvelle génération. Elle rassemble la gestion CRM des contacts (conforme RGPD), l'automatisation comportementale par workflows (moteur de règles & Quartz Scheduler), l'ingestion multi-fournisseurs de webhooks, l'IA générative (Spring AI & Mistral AI) et prédictive (Microservice Python Scikit-Learn), la publication sur réseaux sociaux (chiffrement AES-256) et les tableaux de bord analytiques temps réel (MongoDB).

**Stack** : Java 21 · Spring Boot 3.5 · PostgreSQL · MongoDB · Redis · RabbitMQ · Quartz · Python 3.12 · FastAPI · Scikit-Learn · Docker  
**Organisation** : [SaaS-Teams](https://github.com/SaaS-Teams)

---

## 📋 Table des matières

1. [Architecture Globale & Microservices](#-architecture-globale--microservices)
2. [Prérequis Système](#-prérequis-système)
3. [Installation & Démarrage rapide](#-installation--démarrage-rapide)
4. [Structure du Code Base (Clean Architecture)](#-structure-du-code-base-clean-architecture)
5. [Modules & Responsabilités](#-modules--responsabilités)
6. [Sécurité & Multi-Tenancy (Hibernate Filter & AES-256)](#-sécurité--multi-tenancy-hibernate-filter--aes-256)
7. [Microservice IA Prédictive (FastAPI & Mistral AI)](#-microservice-ia-prédictive-fastapi--mistral-ai)
8. [Variables d'environnement](#-variables-denvironnement)
9. [Documentation API & Endpoints OpenAPI](#-documentation-api--endpoints-openapi)
10. [Conventions, Workflow Git & CI/CD](#-conventions-workflow-git--cicd)

---

## 🏗️ Architecture Globale & Microservices

La plateforme repose sur une architecture découplée et robuste :

- **Monolithe Modulaire Spring Boot 3 (Port 8080)** : Gestion du CRM, des campagnes, de la sécurité multi-tenant, des workflows d'automation et de l'ingestion.
- **Microservice IA Prédictive Python FastAPI (Port 8000)** : Moteur d'inférence ML (Lead Scoring et Churn Risk par Random Forest) et intégration de l'API officielle Mistral AI (`mistral-small-latest`).
- **Services d'Infrastructure** :
  - **PostgreSQL 16** : Base de données relationnelle principale avec filtrage automatique Hibernate par Tenant (`workspace_tracking_id`).
  - **MongoDB 7** : Ingestion à haute fréquence des journaux d'événements et statistiques d'emails/webhooks.
  - **Redis 7** : Cache distribué et Rate Limiting HTTP (Bucket4j).
  - **RabbitMQ 3** : Broker de messages pour les étapes de workflow asynchrones avec gestion de Dead Letter Queue (DLQ).
  - **Quartz Scheduler (JDBC Store)** : Gestion des nœuds de temporisation et de reprise des workflows d'automation.

---

## 📦 Prérequis Système

| Outil | Version minimale |
|:---|:---|
| **Java (JDK)** | 21+ |
| **Python** | 3.10+ |
| **Maven** | 3.9+ |
| **Docker & Docker Compose** | 24+ |
| **Git** | 2.40+ |

---

## ⚙️ Installation & Démarrage rapide

### 1. Démarrer les services d'infrastructure (Docker)

```bash
docker-compose up -d
```

### 2. Démarrer le Backend Spring Boot 3

```bash
# Copier et configurer les variables d'environnement
cp .env.example .env

# Lancer la compilation et le serveur local
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
*Le serveur écoute sur : `http://localhost:8080`*

### 3. Démarrer le Microservice IA Python FastAPI

```bash
cd ../Plateform_Saas_AI_Predictive
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```
*Le microservice écoute sur : `http://localhost:8000`*

---

## 📁 Structure du Code Base (Clean Architecture)

```plaintext
src/main/java/tg/univlome/saas/
├── config/                      # Configurations Spring (Async, Security, RestTemplate, RabbitMQ, Cors, OpenAPI)
├── marketing/
│   ├── ai/                      # IA Générative (Spring AI) & Client HTTP vers Microservice Python (PythonAiPredictiveClient)
│   ├── analytique/              # Pipeline d'agrégation MongoDB pour le Dashboard analytique temps réel
│   ├── automation/              # Moteur d'Automation (Workflows, RuleEngine 7 opérateurs, Quartz Scheduler, RabbitMQ Consumers/DLQ)
│   ├── campagne/                # Gestion des campagnes marketing et suivi de statut
│   ├── contact/                 # CRM Contacts, Segments (Many-to-Many), Tags, Logs de consentement RGPD, Specifications JPA
│   ├── email/                   # Service d'expédition d'emails transactionnels (SendGrid API v3)
│   ├── reseauxsociaux/          # Module Réseaux Sociaux dynamique (SocialAccount chiffré AES-256, SocialPlatformPort, SocialPublishService)
│   └── webhooks/                # Contrôleur d'ingestion de Webhooks (SendGrid, Shopify, Custom) & Inbound Event Consumers
├── shared/                      # Noyau partagé & Infrastructure Multi-Tenant
│   ├── domain/models/           # Entités User, Workspace
│   ├── repositories/            # UserRepository, WorkspaceRepository
│   ├── security/                # JwtUtils, JwtAuthenticationFilter, RateLimitFilter (Bucket4j), CryptoService (AES-256)
│   │   └── tenant/              # Multi-Tenancy (TenantContextHolder, TenantFilter, TenantFilterAspect, TenantListener)
│   └── exceptions/              # Hiérarchie des exceptions globales & GlobalExceptionHandler
└── SaasApplication.java         # Point d'entrée principal Spring Boot 3
```

---

## 📖 Modules & Responsabilités

| Module | Description | Technologies Clés |
|:---|:---|:---|
| `shared.security.tenant` | Isolation stricte des données par espace de travail (Multi-Tenancy). | ThreadLocal, Hibernate 6 `@FilterDef`/`@Filter`, AOP Aspect |
| `shared.security.crypto` | Chiffrement symétrique fort des jetons d'accès sociaux. | AES-256 GCM / CBC, `CryptoService` |
| `marketing.contact` | Gestion CRM des prospects, segmentation dynamique, audit RGPD et import/export CSV. | JPA Specification, RGPD ConsentLog, CSV Mapper |
| `marketing.automation` | Conception et exécution de workflows marketing automatisés sous forme de DAG. | JSONB Postgres, Quartz JDBC, RabbitMQ DLQ |
| `marketing.webhooks` | Réception et traitement réactif d'événements entrants depuis des plateformes externes. | Handlers Webhooks, Inbound Queue |
| `marketing.ai` | Copywriting d'emails marketing par IA et pont HTTP avec le microservice Python. | Spring AI `ChatClient`, `PythonAiPredictiveClient` |
| `marketing.reseauxsociaux` | Module d'outreach et de publication sociale à architecture ouverte (sans Enum). | Pattern Strategy, `SocialPlatformPort`, Spring Registry |
| `marketing.analytique` | Calcul des indicateurs de performance et séries temporelles de conversion. | MongoDB `@Aggregation` Pipeline, REST Dashboard API |

---

## 🛡️ Sécurité & Multi-Tenancy (Hibernate Filter & AES-256)

1. **Multi-Tenancy Transparent** : Chaque requête authentifiée extrait le tenant UUID (`workspaceTrackingId`) via `TenantFilter`. L'aspect AOP `TenantFilterAspect` active automatiquement le filtre Hibernate sur toutes les requêtes Spring Data Repositories.
2. **Chiffrement des Jetons Sociaux (`CryptoService`)** : Les jetons d'accès (`accessToken` et `refreshToken`) enregistrés via `SocialController` sont systématiquement chiffrés en base de données en AES-256 et déchiffrés uniquement en mémoire transitoire au moment de la publication.

---

## 🤖 Microservice IA Prédictive (FastAPI & Mistral AI)

Le microservice Python `Plateform_Saas_AI_Predictive` héberge les modèles d'inférence mathématique et l'intelligence sémantique :

- **Lead Scoring Hybride** : Évalue le score de conversion d'un prospect à partir de son comportement site et applique un boost de **+15.0%** si Mistral AI (`mistral-small-latest`) identifie une intention d'achat.
- **Churn Risk Prediction** : Modèle Random Forest Scikit-Learn calculant la probabilité de désabonnement client (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`).

---

## 🔧 Variables d'environnement

Copier `.env.example` vers `.env` (*ne jamais committer `.env`*).

```properties
# Base de données & Redis
DB_URL=jdbc:postgresql://localhost:5432/saas_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
REDIS_HOST=localhost
REDIS_PORT=6380

# Sécurité & Chiffrement
JWT_SECRET=VotreCleSecreteJWT32OctetsMinimumSecurisee2026!
CRYPTO_SECRET_KEY=SaasMarketingSecretKeyForAES256Encryption2026!

# Intégrations Externes
SENDGRID_API_KEY=SG.xxxxxxxxxxxxxxxxxxxxxx
MISTRAL_API_KEY=your_mistral_api_key_here
```

---

## 📚 Documentation API & Endpoints OpenAPI

Documentation interactive Swagger UI disponible sur : **`http://localhost:8080/swagger-ui.html`**

### Endpoints Principaux

| Catégorie | Méthode | Endpoint | Description |
|:---|:---|:---|:---|
| **Authentification** | `POST` | `/api/v1/auth/register` | Inscription avec auto-provisionnement du Workspace |
| **Authentification** | `POST` | `/api/v1/auth/login` | Connexion et émission du token JWT |
| **CRM Contacts** | `GET` | `/api/v1/contacts` | Liste paginée des contacts filtrés par Tenant |
| **CRM Contacts** | `POST` | `/api/v1/contacts/search` | Recherche dynamique par critères multiples (Specification) |
| **CRM Contacts** | `POST` | `/api/v1/contacts/import` | Importation de contacts en masse par fichier CSV |
| **Automation** | `POST` | `/api/v1/workflows` | Sauvegarde d'un canvas de workflow (JSONB) |
| **Automation** | `POST` | `/api/v1/workflows/{id}/execute` | Déclenchement de l'exécution asynchrone d'un workflow |
| **Webhooks** | `POST` | `/api/v1/webhooks/{provider}` | Ingestion de webhooks externes (SendGrid, Shopify) |
| **IA Générative** | `POST` | `/api/v1/ai/generate-email` | Génération de corps d'email marketing par Spring AI |
| **Social Networks** | `POST` | `/api/v1/social/accounts` | Enregistrement d'un compte social avec chiffrement AES-256 |
| **Social Networks** | `POST` | `/api/v1/social/publish` | Publication d'un message d'outreach sur la plateforme ciblée |
| **Analytics** | `GET` | `/api/v1/analytics/dashboard` | Statistiques globales et séries temporelles de conversion |

---

## 📝 Conventions, Workflow Git & CI/CD

- **Conventions Commits** : Format *Conventional Commits* (`feat(...)`, `fix(...)`, `docs(...)`, `test(...)`).
- **Audit de Code** : Validation obligatoire par Checkstyle (`./mvnw checkstyle:check`) avec **0 violation autorisée**.
- **Couverture de Tests** : JUnit 5 + Mockito + Pytest (Microservice Python).

---

*Plateforme SaaS Marketing — Développé par l'équipe SaaS-Teams*
