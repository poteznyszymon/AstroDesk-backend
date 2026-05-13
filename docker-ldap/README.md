# Docker LDAP + phpLDAPadmin + PostgreSQL + Backup system

## Description
This project runs a Docker-based environment consisting of:
- OpenLDAP
- phpLDAPadmin (web UI for LDAP management)
- PostgreSQL 18
- Alpine:latest (backup system using crontab and postgresql-client)

Includes:
   - `import.sh` script used to import LDAP data from the `user.ldif` file.
   - 'backup.sh' script used to create logical backups and logs to 'backup.log' file
   - 'backup_restore.sh' tool script for restoring actions to database
---



## Project Structure
The project directory must contain:
- `docker-compose.yml`
- `import.sh`
- `user.ldif`
- 'backup.sh'
- '/backups' dir
- '/backups/backup.log'

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
http://localhost:8081
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

## Backup system description

- Backup system runs on alpine:latest using crontab and postgresql-client.
- Crontab is scheduled to execute script every 3 hours
- Postgresql-client is used to create logical backups using pg_dump
- The process is managed by the 'backup.sh' script by creating dumps to ./backups dir and appends execution logs to the ./backups/backup.log file

## Restoring data from logical backup

1. Run script:
   ```bash
   sudo ./backup_restore.sh
   ```
Tool script contains options such as:
   1. Full restore
      - Restores whole data and structure
   WARNING:
      Drops the entire database. Any schema changes deployed after the backup will be permanently lost.

   2. Table restore
      - Resetting isolated tables with no dependencies (Leaf Tables Only)
   WARNING:
      This option must never be used on core tables. This could cause key constraint deletion.

   3. Data-only restore
      - Resetting data without dropping the schema using TRUNCATE CASCADE
   WARNING:
      Always run this on the Root Aggregate (e.g users). If you run it on a child table,
      the restore will fail if its parent record no longer exists in the current database .

   4. Listing content of dump file
      - Displays the dump file structure filtered by keywords (TABLE, VIEW, FUNCTION, SEQUENCE)
   NOTE:
      These keywords can be customized within the script to fit your specific needs.

   5. Restore data to test DB
      - Creates a new, isolated database called test_appdb and restores the data there safely.

   
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

