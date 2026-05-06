#!/bin/sh

set -ex

DATE=$(date +%F_%H-%M)
BACKUP_DIR=/backups
MAX_BACKUPS=10

echo "[ $DATE ]===== EXECUTING SCRIPT ====="
if pg_dump -Fc -C -h "$PGHOST" -U "$PGUSER" -d "$PGDATABASE" > "$BACKUP_DIR/postgres_$DATE.dump"; then
	echo "[ $DATE ]Pg_dump: executed"
else
	echo "[ $DATE ]Pg_dump: failed"
fi

rm -f $(ls -t "$BACKUP_DIR"/postgres_*.dump | tail -n +$((MAX_BACKUPS + 1)))

echo "[ $DATE ]=====   BACKUP DONE  ======"