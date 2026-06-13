# 🚀 SaaS Marketing Platform

Plateforme SaaS de gestion du marketing digital qui aide les entreprises à mieux gérer leur marketing en rassemblant en un seul endroit la gestion des contacts, les campagnes (emails, réseaux sociaux, SMS), l'analyse des résultats et l'automatisation des tâches.

**Stack** : Java 21 · Spring Boot 3.5 · PostgreSQL · JWT · Spring Modulith · Maven  
**Organisation** : [SaaS-Teams](https://github.com/SaaS-Teams)

---

## 📋 Table des matières

1. [Prérequis](#prérequis)
2. [Installation locale](#installation-locale)
3. [Structure du projet](#structure-du-projet)
4. [Modules](#modules)
5. [Conventions — lire absolument](#conventions--lire-absolument)
6. [Workflow Git](#workflow-git)
7. [Pull Requests](#pull-requests)
8. [CI / CD](#ci--cd)
9. [Variables d'environnement](#variables-denvironnement)
10. [Documentation API](#documentation-api)
11. [Contact & ownership des modules](#contact--ownership-des-modules)

---

## 📦 Prérequis

| Outil | Version minimale |
|-------|-----------------|
| Java (JDK) | 21+ |
| Maven | 3.9+ |
| Docker & Docker Compose | 24+ |
| Git | 2.40+ |

---

## ⚙️ Installation locale

```bash
# 1. Cloner le repo
git clone https://github.com/SaaS-Teams/Plateform_Saas_Backend.git
cd Plateform_Saas_Backend

# 2. Démarrer PostgreSQL avec Docker
docker-compose up -d

# 3. Copier le fichier d'environnement
cp .env.example .env
# Remplir les valeurs dans .env (voir section Variables d'environnement)

# 4. Build et lancement
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 5. Vérifier que l'API répond
curl http://localhost:8080/api/health

# 6. Swagger UI
open http://localhost:8080/swagger-ui.html
```

> **Docker doit être démarré** avant de lancer l'application.  
> La base de données est automatiquement créée au démarrage.

---

## 📁 Structure du projet

```
src/main/java/tg/univlome/saas/
├── config/                    # Configuration globale (Security, Swagger, Async)
├── marketing/
│   ├── authentification/      # JWT, gestion utilisateurs — OWNER: Senior
│   ├── contact/               # Gestion des contacts — OWNER: Senior
│   │   ├── application/
│   │   │   ├── controllers/
│   │   │   ├── dtos/
│   │   │   └── mappers/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   ├── services/
│   │   │   └── enums/
│   │   └── repositories/
│   ├── campagne/              # Gestion des campagnes marketing — OWNER: Senior
│   ├── email/                 # Envoi et gestion des emails — OWNER: Junior
│   ├── reseaux_sociaux/       # Intégration réseaux sociaux — OWNER: Junior
│   ├── analytique/            # Tableaux de bord et statistiques — OWNER: Junior
│   └── shared/                # Composants partagés — OWNER: Senior
│       ├── exception/         # GlobalExceptionHandler + hiérarchie exceptions
│       ├── response/          # ApiResponse<T>
│       └── util/              # Classes utilitaires sans état
└── SaasApplication.java
```

---

## 📖 Modules

Le projet est organisé en modules suivant l'architecture **Spring Modulith** :

| Module | Description | Responsable |
|--------|-------------|-------------|
| `authentification` | JWT, authentification, gestion des utilisateurs | Senior |
| `contact` | CRUD contacts, import, segmentation | Senior |
| `campagne` | Création et planification des campagnes | Senior |
| `email` | Envoi d'emails, templates, tracking | Junior |
| `reseaux_sociaux` | Intégration Facebook, Instagram, Twitter | Junior |
| `analytique` | Statistiques, taux d'ouverture, rapports | Junior |
| `shared` | Exceptions, réponses API, utilitaires communs | Senior |

---

## 📝 Conventions — lire absolument

> Voir **[CONTRIBUTING.md](./CONTRIBUTING.md)** pour le détail complet.

**Résumé rapide :**
- Une feature = une branche = une issue GitHub
- Nommage branche : `feature/module-description` ou `fix/module-description`
- Commits : format Conventional Commits (`feat(module):`, `fix(module):`, `test:`, `docs:`, `chore:`)
- Toute PR doit passer le CI avant d'être mergée
- Code review obligatoire : 1 reviewer minimum (Senior review les PRs critiques)
- Jamais de push direct sur `main` ou `develop`

---

## 🌿 Workflow Git

```
main          ← production uniquement, protégée
develop       ← branche d'intégration principale
feature/*     ← nouvelles fonctionnalités
fix/*         ← corrections de bugs
test/*        ← ajout de tests uniquement
docs/*        ← documentation uniquement
chore/*       ← maintenance technique
```

**Cycle de travail quotidien :**

```bash
# 1. Toujours partir de develop à jour
git checkout develop
git pull origin develop

# 2. Créer ta branche depuis develop
git checkout -b feature/contact-import-csv

# 3. Coder, committer régulièrement
git add .
git commit -m "feat(contact): add CSV import with validation"

# 4. Pousser ta branche
git push origin feature/contact-import-csv

# 5. Ouvrir une Pull Request sur GitHub vers develop
```

---

## 🔀 Pull Requests

- **Titre** : `[MODULE] Description courte` → ex: `[CONTACT] Add CSV import`
- **Template** : remplir le template automatique (voir `.github/pull_request_template.md`)
- **Lier l'issue** : mentionner `Closes #42` dans la description
- **Taille** : max 400 lignes de diff — découper si plus grand
- **Tests** : toute PR doit inclure ses tests unitaires
- **Reviewer** : assigner le Senior pour les modules `authentification`, `shared`

---

## 🔄 CI / CD

À chaque Pull Request vers `develop` ou `main` :

1. **Build** : `mvn clean compile`
2. **Tests** : `mvn test` (JUnit 5 + Mockito)
3. **Coverage** : JaCoCo — minimum 70% sur les services

> La PR ne peut pas être mergée si le CI est rouge.

---

## 🔧 Variables d'environnement

Copier `.env.example` → `.env` (**ne jamais committer `.env`**).

| Variable | Description |
|----------|-------------|
| `DB_URL` | URL JDBC PostgreSQL (`jdbc:postgresql://localhost:5432/saas_db`) |
| `DB_USERNAME` | Utilisateur base de données |
| `DB_PASSWORD` | Mot de passe base de données |
| `JWT_SECRET` | Clé secrète JWT (min 256 bits) |
| `JWT_ACCESS_EXPIRATION` | Durée access token en ms (défaut: 900000 = 15min) |
| `JWT_REFRESH_EXPIRATION` | Durée refresh token en ms (défaut: 604800000 = 7j) |
| `MAIL_HOST` | Serveur SMTP (ex: smtp.gmail.com) |
| `MAIL_PORT` | Port SMTP (ex: 587) |
| `MAIL_USERNAME` | Email transactionnel |
| `MAIL_PASSWORD` | Mot de passe SMTP |
| `SMS_API_KEY` | Clé API SMS (à intégrer) |
| `SOCIAL_API_KEY` | Clé API réseaux sociaux (à intégrer) |

---

## 📚 Documentation API

### Swagger UI
Interface interactive disponible après démarrage :

**http://localhost:8080/swagger-ui.html**

### OpenAPI JSON

**http://localhost:8080/api-docs**

### Endpoints principaux

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/health` | Health check |
| `POST` | `/api/v1/auth/login` | Authentification |
| `POST` | `/api/v1/auth/refresh` | Renouveler le token |
| `GET` | `/api/v1/contacts` | Liste des contacts |
| `POST` | `/api/v1/contacts` | Créer un contact |
| `GET` | `/api/v1/campagnes` | Liste des campagnes |
| `POST` | `/api/v1/campagnes` | Créer une campagne |
| `POST` | `/api/v1/campagnes/{id}/envoyer` | Envoyer une campagne |
| `GET` | `/api/v1/analytique/dashboard` | Tableau de bord |

---

## 🤝 Contact & ownership des modules

| Module | Responsable | Rôle |
|--------|-------------|------|
| `authentification`, `contact`, `campagne`, `shared` | À définir | Senior |
| `email`, `reseaux_sociaux`, `analytique` | À définir | Junior |

> En cas de doute sur un module, ouvrir une issue avec le label `question` et tagger le owner.

---

## 🔨 Commandes utiles

```bash
# Nettoyer et compiler
./mvnw clean compile

# Exécuter les tests
./mvnw test

# Construire le projet (sans tests)
./mvnw clean install -DskipTests

# Démarrer l'application (profil dev)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Construire un JAR exécutable
./mvnw clean package

# Démarrer/arrêter Docker
docker-compose up -d
docker-compose down
```


---

*Développé  par l'équipe SaaS Marketing Platform — SaaS-Teams*
