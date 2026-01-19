# Docker LDAP + phpLDAPadmin + PostgreSQL

## Description
This project runs a Docker-based environment consisting of:
- OpenLDAP
- phpLDAPadmin (web UI for LDAP management)
- PostgreSQL 18

It also includes an `import.sh` script used to import LDAP data from the `user.ldif` file.

---



## Project Structure
The project directory must contain:
- `docker-compose.yml`
- `import.sh`
- `user.ldif`

---

## Installation and Startup

1. Navigate to the project directory:
   ```bash
   cd /docker-ldap
   ```

2. Start all containers in detached mode:
   ```bash
   sudo docker-compose up -d
   ```

3. (Optional) Check container status:
   ```bash
   sudo docker-compose ps
   ```

---

## Access to Services

### phpLDAPadmin
Web interface available at:
```
http://localhost:8080
```

LDAP login credentials:
- DN: `cn=admin,dc=astrodesk`
- Password: `adminpassword`

### LDAP
- Port: `389`
- SSL Port: `636`

### PostgreSQL
- Port: `5432`
- User: `admin`
- Password: `adminpassword`
- Database: `astrodesk_db`

---

## LDAP Data Import (`user.ldif`)

Run the import **after containers are started**.

1. Grant execute permissions to the script:
   ```bash
   sudo chmod 751 ./import.sh
   ```

2. Run the import:
   ```bash
   sudo ./import.sh
   ```

The script:
- Copies `user.ldif` into the LDAP container
- Imports entries using `ldapadd`

---

## Stopping and Removing the Environment

To stop containers:
```bash
sudo docker-compose down
```

To stop containers and remove volumes (all data):
```bash
sudo docker-compose down -v
```

WARNING: The `-v` option removes all Docker volumes, including LDAP and PostgreSQL data.

---

## Common Issues
- Ensure `user.ldif` is located in the same directory as `import.sh`
- The LDAP container must be named `ldap`
- If ports 8080, 389, or 5432 are already in use, update port mappings in `docker-compose.yml`

