#!/bin/sh

set -ex

DATE=$(date +%F_%H-%M)
BACKUP_DIR=/backups
LOG_FILE="$BACKUP_DIR/backup.log"

log()
{
	local LEVEL="$1"
	local MESSAGE="$2"
	local TIMESTAMP=$(date +'%Y-%m-%d %H:%M:%S')
	echo "[$TIMESTAMP] [$LEVEL] $MESSAGE" | tee -a "$LOG_FILE"	
}

log "INFO" "===== START BACKUP ====="

CATALOG_NAME=$(date +'%d-%m-%Y')
TARGET_PATH="$BACKUP_DIR/$CATALOG_NAME"
FILE_TIME=$(date +%H-%M) 

if mkdir -p "$TARGET_PATH"; then
	log "INFO" "Katalog docelowy: $TARGET_PATH"
else
	log "ERROR" "Nie można utworzyć katalogu: $TARGET_PATH"
	exit 1
fi

BACKUP_FILE="$TARGET_PATH/postgres_$FILE_TIME.dump"

echo "[ $DATE ]===== EXECUTING SCRIPT ====="
if pg_dump -Fc -C -h "$PGHOST" -U "$PGUSER" -d "$PGDATABASE" > "$BACKUP_FILE"; then
	log "INFO" "Backup utworzony: $BACKUP_FILE"
else
	log "ERROR" "Pg_dump nie utworzyl backupu"
	exit 1
fi

log "INFO" "===== KONIEC ====="
log "INFO" ""
