#!/bin/bash

set -e
POSTGRES_USER="appuser"
POSTGRES_DB="appdb"
DIR=./backups

echo '======== Przywracanie backupu ========'
echo '1. Przywrocenie pelnej bazy danych    '
echo '2. Przywrocenie pelnej tabeli         '
echo '3. Przywrocenie TYLKO danych do tabeli'
echo '4. Pokaz zawartosc backupu            '
echo '5. Przywroc do testowej bazy danych   '
echo '6. Wyjdz                              '

read OPT

case "$OPT" in
	"1") echo -e 'Backup pelny...\n'  &&
	     echo -e 'Podaj nazwe pliku do przywrocenia:\n' &&
	     ls backups | grep .dump
     	     read FILE
	     if [ ! -f "$DIR/$FILE" ]; then
		    echo 'Plik nie istnieje'
		    exit 1
	     fi

	     read -p "Ta operacja USUNIE obecna baze danych. Czy chcesz kontynuowac? (tak/nie): " CONFIRM
	     [ "$CONFIRM" != "tak" ] && exit 1

	     docker-compose exec -i -T postgres pg_restore \
	       -C --clean \
		     -U "$POSTGRES_USER" \
		     -d postgres \
		     < "$DIR/$FILE"

	     echo 'Backup zakonczony' ;;

	"2") echo -e 'Backup danych tabeli...\n'
	     echo -e 'Podaj nazwe pliku do przywrocenia:\n' &&
	     ls backups | grep .dump
	     read FILE
	     if [ ! -f "$DIR/$FILE" ]; then
		     echo 'Plik nie istnieje'
		     exit 1
	     fi
	     echo
	     echo 'Podaj nazwe tabeli'
	     read TABLE
	     TABLE=$(echo "$TABLE" | tr '[:upper:]' '[:lower:]')

	     read -p "Ta operacja USUNIE cala tabele $TABLE. Czy chcesz kontynuowac? (tak/nie): " CONFIRM
	     [ "$CONFIRM" != "tak" ] && exit 1

     	     docker-compose exec -i postgres pg_restore \
		     -U "$POSTGRES_USER" \
		     -d "$POSTGRES_DB" \
		     --clean \
		     --if-exists \
		     -t "$TABLE" \
		     < "$DIR/$FILE"

	    echo "Tabela $TABLE zostala usunieta i przywrocona z backupu" ;;

	"3") echo -e 'Backup TYLKO danych tabeli...\n'
	     echo -e 'Podaj nazwe pliku do przywrocenia:\n' &&
	     ls backups | grep .dump
	     read FILE
	     if [ ! -f "$DIR/$FILE" ]; then
		     echo 'Plik nie istnieje'
		     exit 1
	     fi
	     echo
	     echo 'Podaj nazwe tabeli'
	     read TABLE
	     TABLE=$(echo "$TABLE" | tr '[:upper:]' '[:lower:]')

	     read -p "Ta operacja USUNIE wszelkie rekordy z tabeli. Czy chcesz kontynuowac? (tak/nie): " CONFIRM
	     [ "$CONFIRM" != "tak" ] && exit 1

       DEPENDENCIES=$(docker-compose exec -T postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -t -A -c
	     		  "SELECT DISTINCT conrelid::regclass
              FROM pg_constraint
              WHERE confrelid = 'public.$TABLE'::regclass
              AND contype = 'f';"

	     RESTORE_FLAGS="-t $TABLE"

	     if [ -n "$DEPENDENCIES" ]; then
		     echo "Wykryto powiązane tabele: $DEPENDENCIES"
		     for d in $DEPENDENCIES; do
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
		     < "$DIR/$FILE"

	     echo "Dane z tabeli $TABLE zostala usunieta i przywrocona z backupu"
	     ;;

  "4") echo -e 'Zawartosc backupu'
 	     echo -e 'Podaj nazwe pliku do sprawdzenia:\n' &&
	     ls backups | grep .dump
     	     read FILE
	     if [ ! -f "$DIR/$FILE" ]; then
		    echo 'Plik nie istnieje'
		    exit 1
	     fi

     	     docker-compose exec -i postgres pg_restore -l < "$DIR/$FILE" \
		     | grep -E 'TABLE|VIEW|FUNCTION|SEQUENCE' \
		     | less
	     ;;

	"5") echo -e 'Backup do testowej bazy...\n'  &&
	     echo -e 'Podaj nazwe pliku do przywrocenia:\n' &&
	     ls backups | grep .dump
     	     read FILE
	     if [ ! -f "$DIR/$FILE" ]; then
		    echo 'Plik nie istnieje'
		    exit 1
	     fi

	     read -p "Ta operacja utworzy NOWA baze danych i odtworzy dane
		     z backupu. Czy chcesz kontynuowac? (tak/nie): " CONFIRM
	     [ "$CONFIRM" != "tak" ] && exit 1

             docker-compose exec -T postgres psql -U "$POSTGRES_USER" \
		     -c "DROP DATABASE IF EXISTS test_appdb;"

    	     docker-compose exec -T postgres psql -U "$POSTGRES_USER" \
		     -c "CREATE DATABASE test_appdb;"

             docker-compose exec -i postgres pg_restore \
		     -U "$POSTGRES_USER" \
		     -d test_appdb \
		     < "$DIR/$FILE"


	     echo 'OPERACJA ZAKONCZONA'
	     echo 'Utworzono baze danych: test_appdb'
	     echo "Z backupu: $FILE"
	     ;;

	"6") echo 'wychodzenie' ;;
	*) echo "Bledna operacja"
esac
