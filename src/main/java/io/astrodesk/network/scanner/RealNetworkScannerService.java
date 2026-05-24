package io.astrodesk.network.scanner;

import io.astrodesk.network.dto.NetworkDtos.UpsertNetworkDeviceRequest;
import io.astrodesk.network.service.NetworkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealNetworkScannerService implements NetworkScannerService {

    private final NmapRunner nmap;

    @Lazy
    @Autowired
    private NetworkService networkService;

    @Override
    public void scanNow(List<String> subnets) {
        if (subnets == null || subnets.isEmpty()) {
            log.warn("[RealScanner] No subnets provided, skipping scan");
            return;
        }

        for (String subnet : subnets) {
            log.info("[RealScanner] Scanning subnet: {}", subnet);
            List<DeviceScanResult> results;
            try {
                results = nmap.scan(subnet);
            } catch (Exception e) {
                log.error("[RealScanner] Scan failed for {}: {}", subnet, e.getMessage());
                continue;
            }

            for (DeviceScanResult result : results) {
                String mac = result.macAddress() != null ? result.macAddress() : "IP:" + result.ipAddress();
                String openPorts = formatPorts(result.openPorts());

                UpsertNetworkDeviceRequest req = new UpsertNetworkDeviceRequest(
                        mac,
                        result.ipAddress(),
                        result.hostname(),
                        result.vendor(),
                        null,
                        null,
                        openPorts
                );
                try {
                    networkService.upsertDevice(req);
                } catch (Exception e) {
                    log.warn("[RealScanner] Failed to upsert {}: {}", mac, e.getMessage());
                }
            }
        }
    }

    private String formatPorts(List<DeviceScanResult.OpenPort> ports) {
        if (ports == null || ports.isEmpty()) return null;
        String csv = ports.stream()
                .map(p -> p.port() + "/" + p.service())
                .collect(Collectors.joining(","));
        return csv.length() > 500 ? csv.substring(0, 497) + "..." : csv;
    }
}
