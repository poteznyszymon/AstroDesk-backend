package io.astrodesk.network.scanner;

import io.astrodesk.network.dto.NetworkDtos.UpsertNetworkDeviceRequest;
import io.astrodesk.network.service.NetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@Profile("dev")
public class MockNetworkScannerService implements NetworkScannerService {

    // @Lazy breaks the circular dependency:
    // MockScanner -> NetworkService -> MockScanner
    @Lazy
    @Autowired
    private NetworkService networkService;

    private final Random random = new Random();

    private static final List<MockDevice> MOCK_DEVICES = List.of(
        new MockDevice("Dell",   "LAPTOP-DELL-01",   "A1:B2:C3:D4:E5:F6", "192.168.1.", "SW-FLOOR1", "Gi0/"),
        new MockDevice("HP",     "LAPTOP-HP-02",     "B2:C3:D4:E5:F6:A1", "192.168.1.", "SW-FLOOR1", "Gi0/"),
        new MockDevice("Apple",  "MACBOOK-PRO-03",   "C3:D4:E5:F6:A1:B2", "192.168.1.", "SW-FLOOR2", "Gi1/"),
        new MockDevice("Lenovo", "THINKPAD-04",      "D4:E5:F6:A1:B2:C3", "192.168.1.", "SW-FLOOR2", "Gi1/"),
        new MockDevice("Cisco",  null,               "E5:F6:A1:B2:C3:D4", "192.168.2.", "SW-SERVER", "Gi2/"),
        new MockDevice("Epson",  "PRINTER-EPSON-01", "F6:A1:B2:C3:D4:E5", "192.168.3.", "SW-FLOOR1", "Gi0/"),
        new MockDevice("Dell",   "WORKSTATION-05",   "11:22:33:44:55:66", "192.168.1.", "SW-FLOOR3", "Gi3/"),
        new MockDevice(null,     null,               "AA:BB:CC:DD:EE:FF", "192.168.4.", "SW-WIFI",   "Gi4/")
    );

    @Scheduled(fixedDelayString = "${network.scanner.interval-ms:60000}")
    public void scheduledScan() {
        log.info("[MockScanner] Starting scheduled network scan...");
        scanNow();
    }

    @Override
    public void scanNow() {
        log.info("[MockScanner] Scanning {} mock devices", MOCK_DEVICES.size());

        for (int i = 0; i < MOCK_DEVICES.size(); i++) {
            MockDevice device = MOCK_DEVICES.get(i);
            boolean moved = random.nextInt(5) == 0;

            String ip   = device.ipPrefix()   + (moved ? random.nextInt(50) + 100 : 10 + i);
            String port = device.portPrefix()  + (moved ? random.nextInt(24) + 1   : i + 1);

            UpsertNetworkDeviceRequest req = new UpsertNetworkDeviceRequest(
                device.macAddress(),
                ip,
                device.hostname(),
                device.vendor(),
                device.switchName(),
                port
            );

            try {
                networkService.upsertDevice(req);
                log.debug("[MockScanner] Upserted {} @ {}", device.macAddress(), ip);
            } catch (Exception e) {
                log.warn("[MockScanner] Failed to upsert {}: {}", device.macAddress(), e.getMessage());
            }
        }

        log.info("[MockScanner] Scan complete.");
    }

    private record MockDevice(
        String vendor,
        String hostname,
        String macAddress,
        String ipPrefix,
        String switchName,
        String portPrefix
    ) {}
}
