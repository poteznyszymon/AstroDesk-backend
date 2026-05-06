package io.astrodesk.network.scanner;

import io.astrodesk.network.dto.NetworkDtos.UpsertNetworkDeviceRequest;
import io.astrodesk.network.service.NetworkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@Profile("prod")
@RequiredArgsConstructor
public class RealNetworkScannerService implements NetworkScannerService {

    private final NmapRunner nmapRunner;

    @Lazy
    @Autowired
    private NetworkService networkService;

    @Value("${network.scanner.subnet:192.168.1.0/24}")
    private String subnet;

    @Scheduled(fixedDelayString = "${network.scanner.interval-ms:300000}")
    public void scheduledScan() {
        log.info("[RealScanner] Scheduled scan of {}", subnet);
        scanNow();
    }

    @Override
    public void scanNow() {
        List<NmapScanResult> results;
        try {
            results = nmapRunner.scan(subnet);
        } catch (Exception e) {
            log.error("[RealScanner] nmap failed: {}", e.getMessage());
            return;
        }

        log.info("[RealScanner] Found {} devices", results.size());

        for (NmapScanResult result : results) {
            // switchName/switchPort niedostępne przez nmap (tylko przez SNMP na zarządzalnym switchu)
            UpsertNetworkDeviceRequest req = new UpsertNetworkDeviceRequest(
                    result.macAddress(),
                    result.ipAddress(),
                    result.hostname(),
                    result.vendor(),
                    null,
                    null
            );
            try {
                networkService.upsertDevice(req);
            } catch (Exception e) {
                log.warn("[RealScanner] Failed to upsert {}: {}", result.macAddress(), e.getMessage());
            }
        }
    }
}
