package io.astrodesk.network.scanner;

import java.util.List;

public record NmapScanResult(
        String ipAddress,
        String macAddress,
        String hostname,
        String vendor,
        List<OpenPort> openPorts
) {
    public record OpenPort(int port, String service) {}
}
