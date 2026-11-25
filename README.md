# DriveEasy — Backend Microservices (Spring Boot 3.5 & Spring Cloud 2025.0)

Plateforme de location de véhicules reposant sur une architecture microservices **Spring Boot 3.5 / Spring Cloud 2025.0**, réalisée dans le cadre du module *Développement d'applications web et multimédia* (Licence Informatique et Multimédia — SESAME).

---

## 1. Architecture et ports utilisés

Conformément au cahier des charges (§3 et §4.3), **toutes les données sont chargées et manipulées en mémoire (RAM)** via des dépôts `ConcurrentHashMap` thread-safe ; aucune base de données n'est utilisée. Le fichier `docker-compose.yml` démarre uniquement **Zipkin** pour le traçage distribué.

| Composant | Rôle | Port | URL principale |
|---|---|---:|---|
| `service-registry` | Annuaire Eureka Server (`@EnableEurekaServer`) | **8761** | http://localhost:8761 |
| `config-server` | Configuration centralisée (`@EnableConfigServer`) | **8888** | http://localhost:8888/vehicule-service/dev |
| `vehicule-service` | Gestion du parc automobile, statut et disponibilité | **8081** *(+ 8091 LB)* | http://localhost:8080/api/vehicules |
| `client-service` | Gestion des clients, éligibilité (≥ 21 ans + permis valide), fidélité | **8082** | http://localhost:8080/api/clients |
| `location-service` | Cycle de vie des locations, tarification, pénalités, orchestration Feign | **8083** | http://localhost:8080/api/locations |
| `api-gateway` | Point d'entrée unique (`lb://`), CORS Angular, filtre global de traçage | **8080** | http://localhost:8080 |
| `zipkin` (Docker) | Collecte et visualisation des traces distribuées (Micrometer / Brave) | **9411** | http://localhost:9411 |

---

## 2. Prérequis

- **JDK 21** (Eclipse Temurin / OpenJDK 21)
- **Maven 3.9+** (ou utilisation directe du wrapper `./mvnw` / `.\mvnw.cmd` inclus)
- **Docker** (pour le conteneur Zipkin sur le port `9411`)

---

## 3. Installation et lancement pas à pas

### Étape 1 — Cloner le dépôt et compiler
```powershell
git clone https://github.com/<votre-compte>/driveeasy-backend.git
cd driveeasy-backend
.\mvnw.cmd clean package
```

### Étape 2 — Démarrer Zipkin (Docker)
```powershell
docker compose up -d
```
Interface Zipkin : http://localhost:9411

### Étape 3 — Démarrer les services dans l'ordre recommandé

#### Option A — Démarrage automatique en une commande (Windows PowerShell)
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-all.ps1 -SecondVehiculeInstance
```
- L'option `-SecondVehiculeInstance` démarre une seconde instance de `vehicule-service` sur le port **8091** afin de démontrer le **load balancing** (`lb://vehicule-service`) via l'en-tête HTTP `X-Served-By`.
- Pour arrêter tous les services :
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\stop-all.ps1
```

#### Option B — Démarrage manuel service par service (ordre exigé §3)
1. **Service Registry (8761)** :
   ```powershell
   java -jar service-registry/target/service-registry-1.0.0.jar
   ```
2. **Config Server (8888)** :
   - Mode **GitHub** (par défaut, lit la configuration depuis le dépôt Git) :
     ```powershell
     java -DCONFIG_GIT_URI=https://github.com/<votre-compte>/driveeasy-backend -jar config-server/target/config-server-1.0.0.jar
     ```
   - Mode **local / hors-ligne** (lit le dossier `config-repo/`) :
     ```powershell
     java -Dspring.profiles.active=native -jar config-server/target/config-server-1.0.0.jar
     ```
3. **Microservices métier (8081, 8082, 8083)** :
   ```powershell
   java -jar vehicule-service/target/vehicule-service-1.0.0.jar
   java -jar client-service/target/client-service-1.0.0.jar
   java -jar location-service/target/location-service-1.0.0.jar
   ```
4. **API Gateway (8080)** :
   ```powershell
   java -jar api-gateway/target/api-gateway-1.0.0.jar
   ```

---

## 4. Jeux de données chargés en mémoire au démarrage

Chaque microservice embarque un `DataSeeder` (`CommandLineRunner`, désactivable via `driveeasy.store.seed-enabled=false` en profil `prod`) :

- **12 véhicules** couvrant toutes les catégories (`ECONOMIQUE`, `COMPACTE`, `BERLINE`, `SUV`, `LUXE`), carburants et statuts :
  - `id=2` (*Renault Clio*, `198 TU 5678`) et `id=4` (*Kia Sportage*, `210 TU 3456`) : statut `LOUE` ;
  - `id=8` (*Hyundai Tucson*, `205 TU 8642`) : statut `MAINTENANCE` (réservation refusée en `409`) ;
  - les 9 autres véhicules sont `DISPONIBLE`.
- **8 clients** incluant les cas limites exigés :
  - `id=1` (*Ahmed Ben Ali*, `STANDARD`) : possède déjà **4 locations terminées** et **1 location en cours (`id=5`)** $\rightarrow$ dès le retour de la location `5`, il passe automatiquement en catégorie **`FIDELE`** ;
  - `id=2` (*Fatma Trabelsi*, `FIDELE` — remise 5 %) ;
  - `id=3` (*Mohamed Gharbi*, `VIP` — remise 10 %) ;
  - `id=4` (*Sami Jebali*, **19 ans**) $\rightarrow$ **non éligible** (âge < 21 ans) ;
  - `id=5` (*Ines Mansour*, **permis expiré**) $\rightarrow$ **non éligible**.
- **9 locations** couvrant tous les statuts (`TERMINEE`, `EN_COURS`, `RESERVEE`, `ANNULEE`), dont :
  - `id=5` (`EN_COURS`, client 1, véhicule 2) : prête pour démontrer le retour et la promotion `FIDELE` ;
  - `id=6` (`EN_COURS`, client 2, véhicule 4, **en retard de 2 jours**) : prête pour démontrer le calcul automatique des pénalités ($1{,}5 \times \text{prixJournalier} \times \text{joursRetard}$) ;
  - `id=7` (`RESERVEE`, client 3, véhicule 7) : prête pour démontrer le départ (`PATCH /depart`) ou l'annulation (`PATCH /annulation`).

---

## 5. Tests et collection Postman

- **Tests unitaires JUnit 5** (formule tarifaire, remises `FIDELE`/`VIP`, pénalités de retard) :
  ```powershell
  .\mvnw.cmd test
  ```
- **Collection Postman** : importer `postman/DriveEasy.postman_collection.json` dans Postman (22 requêtes couvrant tous les cas nominaux et les erreurs `400`, `404`, `409`).
- **Swagger UI** de chaque microservice :
  - http://localhost:8081/swagger-ui.html
  - http://localhost:8082/swagger-ui.html
  - http://localhost:8083/swagger-ui.html
