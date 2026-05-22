package io.astrodesk.network.dto;

import java.time.Instant;
import java.util.List;

public class NetworkDtos {

    public record NetworkItemResponse(
            Long id,
            String macAddress,
            String ipAddress,
            String hostname,
            String vendor,
            String switchName,
            String switchPort,
            Instant lastSeenAt
    ) {}

    public record NetworkHistoryResponse(
            Long id,
            String macAddress,
            String ipAddress,
            String switchName,
            String switchPort,
            Instant seenAt
    ) {}

    public record NetworkDeviceDetail(
            NetworkItemResponse device,
            List<NetworkHistoryResponse> history
    ) {}

    public record UpsertNetworkDeviceRequest(
            String macAddress,
            String ipAddress,
            String hostname,
            String vendor,
            String switchName,
            String switchPort
    ) {}

    public record NetworkDeviceFilter(
            String hostname,
            String macAddress,
            String switchName,
            List<String> vendors
    ) {}

    public record AvailableSubnetResponse(
            String interfaceName,
            String subnet
    ) {}

    public record ScanRequest(
            List<String> subnets
    ) {}
}
