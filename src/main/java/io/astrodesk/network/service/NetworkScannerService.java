package io.astrodesk.network.scanner;

/**
 * Abstrakcja skanera sieci.
 *
 * W trybie dev: MockNetworkScannerService  (@Profile("dev"))
 * W produkcji:  RealNetworkScannerService  (@Profile("prod")) — do zaimplementowania
 *               np. przez wywołanie nmap lub odpytanie switcha przez SNMP.
 */
public interface NetworkScannerService {
    void scanNow();
}
