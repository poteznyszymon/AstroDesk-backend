#!/bin/bash

set -e
POSTGRES_USER="appuser"
POSTGRES_DB="appdb"
DIR=./backups

choose_file() 
{
    echo -e 'Podaj katalog, z ktorego dnia chcesz przywrocic backup:'
    ls "$DIR" -I backup.log
    read -r CATALOG
    
    if [ ! -d "$DIR/$CATALOG" ] || [ -z "$CATALOG" ]; then
        echo "Błąd: Katalog '$CATALOG' nie istnieje."
        exit 1
    fi

    echo -e 'Podaj nazwe pliku do przywrocenia:'
    ls "$DIR/$CATALOG" | grep .dump
    read -r FILE
    
    FULL_PATH="$DIR/$CATALOG/$FILE"
    
    if [ ! -f "$FULL_PATH" ]; then
        echo "Plik '$FULL_PATH' nie istnieje"
        exit 1
    fi
}

echo '======== Przywracanie backupu ========'
echo '1. Przywrocenie pelnej bazy danych    '
echo '2. Przywrocenie pelnej tabeli         '
echo '3. Przywrocenie TYLKO danych do tabeli'
echo '4. Pokaz zawartosc backupu            '
echo '5. Przywroc do testowej bazy danych   '
echo '6. Wyjdz                              '

read OPT

case "$OPT" in
	"1") echo -e 'Przywrocenie pelnej bazy danych...\n'  &&
	     
	     choose_file

	     read -p "Ta operacja USUNIE obecna baze danych. Czy chcesz kontynuowac? (tak/nie): " CONFIRM
	     [ "$CONFIRM" != "tak" ] && exit 1

	     echo "Odcinam aktywne polaczenia do bazy $POSTGRES_DB..."

     	     docker-compose exec -T postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c \
		     "SELECT pg_terminate_backend(pid) FROM pg_stat_activity 
	     	      WHERE datname = '$POSTGRES_DB' AND pid <> pg_backend_pid();" > /dev/null

	     echo "Przywracanie danych..."
	     docker-compose exec -i -T postgres pg_restore \
	       -C --clean \
		     -U "$POSTGRES_USER" \
		     -d postgres \
		     < "$FULL_PATH"

	     echo 'Proces zakonczony' ;;

	"2") echo -e 'Przywrocenie pelnej tabeli...\n'
		
	     choose_file
			     
	     echo 'Podaj nazwe tabeli'
	     read TABLE
	     TABLE=$(echo "$TABLE" | tr '[:upper:]' '[:lower:]')

	     read -p "Ta operacja USUNIE cala tabele $TABLE. Czy chcesz kontynuowac? (tak/nie): " CONFIRM
	     [ "$CONFIRM" != "tak" ] && exit 1

	     docker-compose exec -T postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
		     -c "DROP TABLE IF EXISTS $TABLE;"

     	     docker-compose exec -i -T postgres pg_restore \
		     -U "$POSTGRES_USER" \
		     -d "$POSTGRES_DB" \
		     --clean \
		     --if-exists \
		     -t "$TABLE" \
		     < "$FULL_PATH"

	    echo "Tabela $TABLE zostala usunieta i przywrocona z backupu" ;;

	"3") echo -e 'Backup TYLKO danych tabeli...\n'
	     
 	     choose_file

	     echo 'Podaj nazwe tabeli'
	     read TABLE
	     TABLE=$(echo "$TABLE" | tr '[:upper:]' '[:lower:]')

	     read -p "Ta operacja USUNIE wszelkie rekordy z tabeli. Czy chcesz kontynuowac? (tak/nie): " CONFIRM
	     [ "$CONFIRM" != "tak" ] && exit 1

       DEPENDENCIES=$(docker-compose exec -T postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -t -A -c \
	     		  "SELECT DISTINCT conrelid::regclass
              FROM pg_constraint
              WHERE confrelid = 'public.$TABLE'::regclass
	      AND contype = 'f';")

	     docker-compose exec -T postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
		     -c "TRUNCATE TABLE $TABLE CASCADE;"

	     RESTORE_FLAGS="-t $TABLE"

	     if [ -n "$DEPENDENCIES" ]; then
		     echo "Wykryto powiązane tabele: $DEPENDENCIES"
		     for DEP in $DEPENDENCIES; do
			     RESTORE_FLAGS="$RESTORE_FLAGS -t $DEP"
		     done
	     else
		     echo 'Brak powiazanych tabel'
	     fi

	     echo "Przywracanie danych dla tabel: $RESTORE_FLAGS"

     	 docker-compose exec -i -T postgres pg_restore \
		     -U "$POSTGRES_USER" \
		     -d "$POSTGRES_DB" \
		     --data-only \
		     $RESTORE_FLAGS \
		     --disable-triggers \
		     < "$FULL_PATH"

	     echo "Dane z tabeli $TABLE zostala usunieta i przywrocona z backupu"
	;;
  "4") echo -e 'Zawartosc backupu'
	     choose_file

     	     docker-compose exec -i -T postgres pg_restore -l < "$FULL_PATH" \
		     | grep -E 'TABLE|VIEW|FUNCTION|SEQUENCE' \
		     | less
	     ;;

	"5") echo -e 'Backup do testowej bazy...\n'  &&
     	     choose_file

	     read -p "Ta operacja utworzy NOWA baze danych i odtworzy dane
		     z backupu. Czy chcesz kontynuowac? (tak/nie): " CONFIRM
	     [ "$CONFIRM" != "tak" ] && exit 1

             docker-compose exec -T postgres psql -U "$POSTGRES_USER" \
		     -c "DROP DATABASE IF EXISTS test_appdb;"

    	     docker-compose exec -T postgres psql -U "$POSTGRES_USER" \
		     -c "CREATE DATABASE test_appdb;"

             docker-compose exec -i -T postgres pg_restore \
		     -U "$POSTGRES_USER" \
		     -d test_appdb \
		     < "$FULL_PATH"


	     echo 'OPERACJA ZAKONCZONA'
	     echo 'Utworzono baze danych: test_appdb'
	     echo "Z backupu: $FILE"
	     ;;

	"6") echo 'wychodzenie' ;;
	*) echo "Bledna operacja"
esac
