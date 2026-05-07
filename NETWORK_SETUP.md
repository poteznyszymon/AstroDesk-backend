# AstroDesk Backend — uruchomienie modułu sieci

## Wymagania

- Java 17+
- Docker Desktop (uruchomiony)
- [nmap](https://nmap.org/download.html) zainstalowany na Windowsie

> **Ważne:** terminal z backendem musi być uruchomiony **jako Administrator**,
> żeby nmap mógł odczytywać adresy MAC urządzeń.

---

## Konfiguracja przed uruchomieniem

Edytuj plik `src/main/resources/application-prod.properties`:

```properties
# Pełna ścieżka do nmap.exe
network.scanner.nmap-path=C:/Program Files (x86)/Nmap/nmap.exe

# Podsieć do skanowania — wpisz podsieć swojej sieci lokalnej
# Sprawdź swój adres IP przez: ipconfig
# Np. jeśli masz 192.168.1.x to wpisz 192.168.1.0/24
network.scanner.subnet=192.168.1.0/24

# Jak często skanować automatycznie (ms) — domyślnie 5 minut
network.scanner.interval-ms=300000
```

---

## Uruchomienie

**1. Uruchom Docker (baza danych + LDAP):**
```bat
cd docker-ldap
docker compose up -d
cd ..
```

**2. Uruchom backend (jako Administrator):**
```bat
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod
```

**3. Wyzwól pierwszy skan ręcznie:**
```bat
curl -X POST http://localhost:8080/api/network/scan
```

Po chwili urządzenia pojawią się na stronie w zakładce Sieć.

---

## Jak sprawdzić swoją podsieć

```bat
ipconfig
```

Szukaj karty WiFi lub Ethernet — np.:
```
IPv4 Address: 192.168.1.30
Subnet Mask:  255.255.255.0
```

Twoja podsieć to `192.168.1.0/24`.
