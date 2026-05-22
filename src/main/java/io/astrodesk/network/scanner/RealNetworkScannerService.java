package io.astrodesk.network.scanner;

import io.astrodesk.network.dto.NetworkDtos.UpsertNetworkDeviceRequest;
import io.astrodesk.network.service.NetworkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Profile("!dev")
@RequiredArgsConstructor
public class RealNetworkScannerService implements NetworkScannerService {

    private final NmapRunner nmapRunner;
    private final NetworkEnricher enricher;

    @Lazy
    @Autowired
    private NetworkService networkService;

    @Override
    public void scanNow(List<String> subnets) {
        if (subnets == null || subnets.isEmpty()) {
            log.warn("[RealScanner] No subnets provided, skipping scan");
            return;
        }

        Map<String, String> arpTable = enricher.getArpTable();
        log.info("[RealScanner] ARP table: {} entries", arpTable.size());

        for (String subnet : subnets) {
            log.info("[RealScanner] Scanning subnet: {}", subnet);
            List<NmapScanResult> results;
            try {
                results = nmapRunner.scan(subnet);
            } catch (Exception e) {
                log.error("[RealScanner] nmap failed for {}: {}", subnet, e.getMessage());
                continue;
            }

            log.info("[RealScanner] Found {} devices in {}", results.size(), subnet);

            for (NmapScanResult result : results) {
                String mac    = resolveMac(result.ipAddress(), arpTable);
                String vendor = result.vendor() != null ? result.vendor() : enricher.lookupVendor(mac);

                UpsertNetworkDeviceRequest req = new UpsertNetworkDeviceRequest(
                        mac,
                        result.ipAddress(),
                        result.hostname(),
                        vendor,
                        null,
                        null
                );
                try {
                    networkService.upsertDevice(req);
                } catch (Exception e) {
                    log.warn("[RealScanner] Failed to upsert {}: {}", mac, e.getMessage());
                }
            }
        }
    }

    private String resolveMac(String ip, Map<String, String> arpTable) {
        // 1. Own interface
        String mac = enricher.getMacForLocalIp(ip);
        if (mac != null) return mac;
        // 2. ARP cache
        mac = arpTable.get(ip);
        if (mac != null) return mac;
        // 3. Fallback
        return "IP:" + ip;
    }
}
