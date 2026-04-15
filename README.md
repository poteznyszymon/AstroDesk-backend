# AstroDesk Backend

Spring Boot REST API for the AstroDesk helpdesk system.

**Stack:** Java 17 · Spring Boot 4 · Spring Security (session-based) · Spring Data JPA · PostgreSQL · LDAP

---

## Prerequisites

- Java 17+
- Maven (or use the included `./mvnw`)
- Docker + Docker Compose

---

## Local Setup

### 1. Start infrastructure (PostgreSQL + LDAP)

```bash
cd docker-ldap
docker compose up -d
```

This starts:
| Service | URL / Port |
|---|---|
| PostgreSQL | `localhost:5432` |
| OpenLDAP | `localhost:389` |
| phpLDAPadmin | http://localhost:8081 |

### 2. Import LDAP users

**Linux/macOS:**
```bash
cd docker-ldap
chmod +x import.sh
./import.sh
```

**Windows:**
```bat
cd docker-ldap
import.bat
```

> Run this **once** after first `docker compose up`. The script copies `user.ldif` into the LDAP container and imports all users and groups.

### 3. Run the backend

From the project root:

```bash
./mvnw spring-boot:run
```

API is available at **http://localhost:8080**

---

## Test Users

All users share the same password: **`Password123!`**

| Login | Name | Role |
|---|---|---|
| `jan` | Jan Kowalski | HEADADMIN |
| `ania` | Anna Nowak | HEADADMIN |
| `marta` | Marta Kaminska | TICKET_ADMIN |
| `kasia` | Katarzyna Wisniewska | TICKET_ADMIN |
| `kazimierz` | Kazimierz Kowalczyk | ASSET_ADMIN |
| `maciej` | Maciej Piekarski | ASSET_ADMIN |
| `grzegorz` | Grzegorz Gajewski | USER |
| `laura` | Laura Mazurska | USER |
| `piotr` | Piotr Zielinski | USER |

**Role permissions:**
- `HEADADMIN` — full access (tickets + assets)
- `TICKET_ADMIN` — manage tickets
- `ASSET_ADMIN` — manage inventory
- `USER` — create tickets, view own data

---

## Configuration

`src/main/resources/application.properties` — no changes needed for local dev, all defaults match the Docker Compose setup:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/appdb
spring.datasource.username=appuser
spring.datasource.password=apppassword

ldap.url=ldap://localhost:389
ldap.base=dc=astrodesk
ldap.dn=cn=admin,dc=astrodesk
ldap.pwd=adminpassword
```

---

## Stopping

```bash
# Stop containers (keeps data)
docker compose -f docker-ldap/docker-compose.yml down

# Stop and wipe all data (DB + LDAP volumes)
docker compose -f docker-ldap/docker-compose.yml down -v
```

> After `down -v` you need to re-run the LDAP import script on next startup.

---

## phpLDAPadmin

Web UI for browsing/editing LDAP entries: http://localhost:8081

Login:
- DN: `cn=admin,dc=astrodesk`
- Password: `adminpassword`
