# Guide de contribution — SaaS Marketing Platform

> Ce fichier est **obligatoire à lire** avant d'écrire la moindre ligne de code.  
> Il définit les règles de travail pour l'équipe backend du projet.

---

## Table des matières

1. [Philosophie](#1-philosophie)
2. [Branches Git](#2-branches-git)
3. [Conventions de commits](#3-conventions-de-commits)
4. [Conventions de code Java](#4-conventions-de-code-java)
5. [Architecture — règles absolues](#5-architecture--règles-absolues)
6. [Nommage](#6-nommage)
7. [Format de réponse API](#7-format-de-réponse-api)
8. [Gestion des erreurs](#8-gestion-des-erreurs)
9. [Tests — obligatoires](#9-tests--obligatoires)
10. [Pull Request process](#10-pull-request-process)
11. [GitHub Issues & Project](#11-github-issues--project)
12. [Ce qu'il ne faut JAMAIS faire](#12-ce-quil-ne-faut-jamais-faire)

---

## 1. Philosophie

- **Chaque module est une île.** Aucune dépendance directe entre modules — on communique via des services exposés, jamais en appelant directement un repository d'un autre module.
- **Les controllers ne pensent pas.** Toute logique métier est dans les services.
- **Les entités JPA ne voyagent pas.** On n'expose jamais une entité dans une réponse HTTP — on utilise des DTOs.
- **Un test par comportement.** Les tests vérifient ce que le code fait, pas comment il le fait.

---

## 2. Branches Git

### Stratégie de branches

```
main        ← production uniquement. Jamais de push direct. Merge depuis develop via PR.
develop     ← intégration. C'est ici qu'on merge nos features. CI automatique.
feature/*   ← développement d'une fonctionnalité
fix/*       ← correction d'un bug
test/*      ← ajout de tests sans changement fonctionnel
docs/*      ← mise à jour de documentation uniquement
chore/*     ← tâches techniques (config, dépendances, CI)
```

### Règles de nommage des branches

Format : `type/module-description-courte`

```bash
# Exemples corrects
feature/authentification-jwt-refresh-token
feature/contact-import-csv
feature/campagne-scheduler-email
fix/email-prevent-duplicate-send
test/contact-service-unit
docs/api-endpoints-update
chore/flyway-v2-migration-contacts

# Exemples incorrects
feature/ma-feature        # trop vague
fix/bug                   # pas descriptif
Feature/Auth              # majuscule interdite
```

### Commandes de base

```bash
# Toujours synchroniser develop avant de créer une branche
git checkout develop
git pull origin develop
git checkout -b feature/mon-module-ma-feature

# Pousser la branche
git push origin feature/mon-module-ma-feature

# Mettre à jour sa branche depuis develop (évite les conflits)
git fetch origin
git rebase origin/develop
```

---

## 3. Conventions de commits

On suit le standard **Conventional Commits** : `type(scope): message`

### Types autorisés

| Type | Usage |
|------|-------|
| `feat` | Nouvelle fonctionnalité |
| `fix` | Correction de bug |
| `test` | Ajout ou modification de tests |
| `docs` | Documentation uniquement |
| `refactor` | Refactoring sans changement de comportement |
| `chore` | Tâche technique (dépendances, config, migration) |
| `perf` | Amélioration de performance |

### Scope = nom du module

`authentification` · `contact` · `campagne` · `email` · `reseaux_sociaux` · `analytique` · `shared` · `config` · `ci`

### Format du message

```
type(scope): description courte en minuscules, présent, sans point final

Corps optionnel (après une ligne vide) :
Expliquer le POURQUOI si la décision n'est pas évidente.
Maximum 72 caractères par ligne.

Closes #42
```

### Exemples corrects

```bash
git commit -m "feat(authentification): implement JWT access token with 15min expiration"
git commit -m "feat(authentification): add refresh token endpoint with httpOnly cookie"
git commit -m "fix(email): prevent duplicate send on network timeout"
git commit -m "test(contact): add unit tests for ContactServiceImpl.createContact"
git commit -m "chore(config): add Flyway V2 migration for contacts table"
git commit -m "docs(api): update Swagger description for /api/v1/contacts"
git commit -m "feat(campagne): add campaign scheduler with cron expression"
```

### Exemples incorrects

```bash
git commit -m "update"              # pas de type, pas de scope, trop vague
git commit -m "Fix bug"             # majuscule, pas de scope
git commit -m "feat: added things"  # passé composé, pas de scope
git commit -m "WIP"                 # ne jamais committer un WIP sur develop
```

---

## 4. Conventions de code Java

### Annotations de validation sur tous les DTOs de requête

```java
// Exemple CreateContactRequest
public record CreateContactRequest(
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100)
    String nom,

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100)
    String prenom,

    @NotBlank
    @Email(message = "L'email doit être valide")
    String email,

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Numéro de téléphone invalide")
    String telephone,

    @Size(max = 200)
    String entreprise,

    @Size(max = 100)
    String poste
) {}
```

### Javadoc obligatoire sur toutes les méthodes publiques des services

```java
/**
 * Crée un nouveau contact pour la plateforme marketing.
 *
 * @param request  les données du contact à créer
 * @return le contact créé sous forme de DTO
 * @throws ConflictException si un contact avec le même email existe déjà
 */
ContactResponse createContact(CreateContactRequest request);
```

### Logger SLF4J — un par classe

```java
// Déclaration (toujours private static final)
private static final Logger log = LoggerFactory.getLogger(ContactServiceImpl.class);

// Usage
log.info("Contact created: id={}, email={}", contact.getId(), contact.getEmail());
log.warn("Duplicate contact attempt for email={}", request.email());
log.error("Failed to send campaign id={}", campagneId, e);

// INTERDIT
System.out.println("...");  // jamais
e.printStackTrace();         // jamais
```

### Transactions — uniquement dans les services

```java
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    // Lecture : readOnly=true pour optimisation Hibernate
    @Transactional(readOnly = true)
    public ContactResponse findById(Long id) { ... }

    // Écriture : @Transactional sans readOnly
    @Transactional
    public ContactResponse createContact(CreateContactRequest request) { ... }
}
```

### Pas de FetchType.EAGER sans justification

```java
// Par défaut — toujours LAZY
@OneToMany(mappedBy = "campagne", fetch = FetchType.LAZY)
private List<Contact> contacts;

// Si besoin de charger systématiquement — justifier en commentaire
@ManyToOne(fetch = FetchType.EAGER) // Toujours chargé : utilisé dans 100% des appels
private Campagne campagne;
```

---

## 5. Architecture — règles absolues

### Structure d'un module Spring Modulith

```
marketing/{module}/
├── application/
│   ├── controllers/    # @RestController — délègue au service, zero logique
│   ├── dtos/           # Records ou classes simples — entrée/sortie API
│   └── mappers/        # @Mapper MapStruct — entité ↔ DTO
├── domain/
│   ├── models/         # @Entity JPA — ne sort JAMAIS du repository
│   ├── services/       # Interface + Impl — toute la logique métier ici
│   └── enums/          # Énumérations du domaine
└── repositories/       # JpaRepository — requêtes uniquement
```

### Règles de dépendance entre modules

```
✅ Autorisé
CampagneService     → (inject) ContactService      # via l'interface
EmailService        → (inject) CampagneService     # via l'interface
AnalytiqueService   → (inject) CampagneService     # via l'interface

❌ Interdit
CampagneServiceImpl → (inject) ContactRepository   # jamais le repo d'un autre module
EmailController     → (inject) ContactRepository   # jamais
```

### Controller — délégation pure

```java
@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContactResponse>> getById(@PathVariable Long id) {
        // Un seul appel au service, rien d'autre
        return ResponseEntity.ok(ApiResponse.success(contactService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContactResponse>> create(
            @Valid @RequestBody CreateContactRequest request) {
        ContactResponse response = contactService.createContact(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
```

---

## 6. Nommage

### Classes Java

| Élément | Convention | Exemple |
|---------|-----------|---------|
| Entités JPA | PascalCase | `Contact`, `Campagne`, `Email` |
| DTOs requête | `{Nom}Request` | `CreateContactRequest` |
| DTOs réponse | `{Nom}Response` | `ContactResponse` |
| Interface service | `{Nom}Service` | `ContactService` |
| Implémentation | `{Nom}ServiceImpl` | `ContactServiceImpl` |
| Repository | `{Nom}Repository` | `ContactRepository` |
| Mapper | `{Nom}Mapper` | `ContactMapper` |
| Constantes | UPPER_SNAKE_CASE | `MAX_RECIPIENTS_PER_CAMPAIGN` |

### Endpoints REST

```
GET    /api/v1/contacts              # Liste paginée
GET    /api/v1/contacts/{id}         # Détail
POST   /api/v1/contacts              # Création
PUT    /api/v1/contacts/{id}         # Mise à jour complète
PATCH  /api/v1/contacts/{id}         # Mise à jour partielle
DELETE /api/v1/contacts/{id}         # Suppression

GET    /api/v1/campagnes/{id}/contacts   # Ressources imbriquées
POST   /api/v1/campagnes/{id}/envoyer    # Action avec verbe
```

### Base de données

```sql
-- Tables : snake_case, pluriel
CREATE TABLE contacts (...);
CREATE TABLE campagnes (...);
CREATE TABLE campagne_contacts (...);

-- Colonnes : snake_case
created_at, updated_at, is_active, campagne_id

-- Index : idx_{table}_{colonne}
CREATE INDEX idx_contacts_email ON contacts(email);
CREATE INDEX idx_campagnes_statut ON campagnes(statut);

-- Contraintes unique : uk_{table}_{colonne}
ALTER TABLE contacts ADD CONSTRAINT uk_contacts_email UNIQUE (email);

-- Clés étrangères : fk_{table}_{ref}
ALTER TABLE campagne_contacts ADD CONSTRAINT fk_campagne_contacts_campagne
  FOREIGN KEY (campagne_id) REFERENCES campagnes(id);
```

### Migrations Flyway

```
V1__init_schema.sql
V2__create_contacts_table.sql
V3__create_campagnes_table.sql
V4__create_emails_table.sql
V5__create_analytique_table.sql

# Règle absolue : jamais modifier une migration existante.
# Si correction nécessaire → créer une nouvelle migration Vn+1.
```

---

## 7. Format de réponse API

**Toutes** les réponses utilisent `ApiResponse<T>` défini dans `shared/`.

### Succès

```json
{
  "success": true,
  "message": "Contact créé avec succès",
  "data": {
    "id": 1,
    "nom": "Dupont",
    "email": "dupont@example.com"
  },
  "timestamp": "2025-06-13T10:00:00Z"
}
```

### Erreur

```json
{
  "success": false,
  "message": "Ressource introuvable",
  "errorCode": "CONTACT_NOT_FOUND",
  "details": ["Le contact avec l'ID 99 n'existe pas."],
  "timestamp": "2025-06-13T10:00:00Z"
}
```

### Règles

- Codes d'erreur en `UPPER_SNAKE_CASE` : `CONTACT_NOT_FOUND`, `EMAIL_ALREADY_EXISTS`, `CAMPAIGN_ALREADY_SENT`
- Messages en français pour les erreurs métier visibles par l'utilisateur
- Ne jamais exposer de stack trace dans la réponse
- Listes paginées : envelopper dans `ApiResponse<Page<T>>`

---

## 8. Gestion des erreurs

### Hiérarchie des exceptions

```java
BaseException (abstract, RuntimeException)
├── ResourceNotFoundException  → HTTP 404
├── BusinessRuleException      → HTTP 422
├── ConflictException          → HTTP 409
├── UnauthorizedException      → HTTP 401
└── ForbiddenException         → HTTP 403
```

### Usage

```java
// Dans un service
public ContactResponse findById(Long id) {
    return contactRepository.findById(id)
        .map(contactMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("CONTACT_NOT_FOUND",
            "Le contact avec l'ID " + id + " n'existe pas."));
}

// Règle métier
if (campagne.isDejaEnvoyee()) {
    throw new BusinessRuleException("CAMPAIGN_ALREADY_SENT",
        "Cette campagne a déjà été envoyée et ne peut pas être modifiée.");
}
```

---

## 9. Tests — obligatoires

### Toute PR doit inclure les tests de ce qu'elle ajoute ou modifie.

### Tests unitaires (service layer)

```java
@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

    @Mock private ContactRepository contactRepository;
    @Mock private ContactMapper contactMapper;
    @InjectMocks private ContactServiceImpl contactService;

    @Test
    void shouldReturnContactWhenExists() {
        // Given
        Contact contact = new Contact();
        contact.setId(1L);
        given(contactRepository.findById(1L)).willReturn(Optional.of(contact));
        given(contactMapper.toResponse(contact)).willReturn(new ContactResponse(1L, "Dupont", ...));

        // When
        ContactResponse result = contactService.findById(1L);

        // Then
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void shouldThrowResourceNotFoundWhenContactDoesNotExist() {
        given(contactRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> contactService.findById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("CONTACT_NOT_FOUND");
    }
}
```

### Nommage des tests

```
should{Comportement}When{Condition}

shouldReturnContactWhenExists
shouldThrowResourceNotFoundWhenContactDoesNotExist
shouldSendCampaignWhenAllRecipientsValid
shouldIncrementOpenRateWhenEmailOpened
```

### Coverage minimum

- Services : **70%** minimum (vérifié par JaCoCo dans le CI)
- Controllers : tester avec MockMvc les cas nominaux et les cas d'erreur
- Pas de test sur les mappers MapStruct (générés automatiquement)

---

## 10. Pull Request process

### Avant d'ouvrir une PR

```bash
# S'assurer que develop est intégré
git fetch origin
git rebase origin/develop

# Vérifier que les tests passent en local
mvn clean test

# Vérifier le build
mvn clean package -DskipTests
```

### Titre de la PR

```
[MODULE] Description courte en français

Exemples :
[AUTHENTIFICATION] Implement JWT access token and refresh token
[CONTACT] Add contact creation with email validation
[CAMPAGNE] Add campaign scheduler and sending workflow
[EMAIL] Add duplicate send prevention on timeout
[ANALYTIQUE] Add open rate and click rate tracking
```

### Reviews

| Auteur PR | Reviewer requis |
|-----------|-----------------|
| Junior | Senior |
| Senior | Auto-approuvé si CI vert pour les features isolées |

Les modules `authentification` et `shared` → **toujours reviewés par le Senior**.

### Merge

- **Squash and merge** sur `develop` (un commit propre par feature)
- Le titre du commit de merge = titre de la PR
- Supprimer la branche après merge

---

## 11. GitHub Issues & Project

### Créer une issue avant de coder

Chaque tâche doit avoir une issue GitHub avant de créer une branche.

### Labels à utiliser

**Type :**
`feat` · `bug` · `test` · `docs` · `chore` · `question`

**Module :**
`authentification` · `contact` · `campagne` · `email` · `reseaux_sociaux` · `analytique` · `shared` · `infra` · `ci`

**Priorité :**
`priority:critical` · `priority:high` · `priority:medium` · `priority:low`

**Owner :**
`owner:senior` · `owner:junior`

### Colonnes du GitHub Project

```
📋 Backlog        ← toutes les issues futures
🎯 Sprint actuel  ← issues de la semaine en cours
🔄 In Progress    ← en cours de développement (assignée + branche créée)
👀 In Review      ← PR ouverte, en attente de review
✅ Done           ← mergée sur develop
```

### Règle : déplacer soi-même ses cartes

- Quand tu commences → **In Progress**
- Quand tu ouvres la PR → **In Review**
- Quand la PR est mergée → **Done**

---

## 12. Ce qu'il ne faut JAMAIS faire

```java
// ❌ Logique métier dans un controller
if (contact.getEmail() == null) return ResponseEntity.badRequest()... // NON

// ❌ Retourner une entité JPA dans un controller
return ResponseEntity.ok(contactRepository.findById(id)); // NON — utiliser un DTO

// ❌ System.out.println
System.out.println("Contact created: " + contact.getId()); // NON — utiliser log.info(...)

// ❌ Valeurs hardcodées
String apiKey = "sk_live_xxxxxxxx"; // NON — variable d'environnement

// ❌ @Transactional dans un controller ou un repository
@Transactional // NON — uniquement dans les services
public ResponseEntity<...> create(...) { ... }

// ❌ Appeler un repository d'un autre module
@Autowired
private ContactRepository contactRepository; // NON dans CampagneServiceImpl — passer par ContactService

// ❌ FetchType.EAGER sans justification
@OneToMany(fetch = FetchType.EAGER) // NON par défaut

// ❌ Modifier une migration Flyway existante
// V2__create_contacts_table.sql → NE JAMAIS MODIFIER après le premier run

// ❌ Committer des secrets
DB_PASSWORD=monmotdepasse123 // NON dans le code ou dans .env commité

// ❌ Push direct sur main ou develop
git push origin main // BLOQUÉ par GitHub — toujours passer par une PR
```

---

*Document maintenu par le Senior. Toute suggestion d'amélioration → ouvrir une issue avec le label `docs`.*
