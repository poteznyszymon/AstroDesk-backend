package io.astrodesk.network.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

// ─── Response DTOs ──────────────────────────────────────────────────────────

public class NetworkDtos {

    /** Full device row — matches the NetworkItem type from the frontend. */
    public record NetworkItemResponse(
            Long id,
            String macAddress,
            String ipAddress,
            String hostname,
            String vendor,
            String switchName,
            String switchPort,
            Instant lastSeenAt,
            @JsonProperty("isImported") boolean isImported,
            String linkedAssetName
    ) {}

    /** Single history entry — matches NetworkHistory type from the frontend. */
    public record NetworkHistoryResponse(
            Long id,
            String macAddress,
            String ipAddress,
            String switchName,
            String switchPort,
            Instant seenAt
    ) {}

    /** Device detail with embedded history list. */
    public record NetworkDeviceDetail(
            NetworkItemResponse device,
            List<NetworkHistoryResponse> history
    ) {}

    // ─── Request DTOs ────────────────────────────────────────────────────────

    /**
     * Sent by the network scanner (or manually) to upsert a device.
     * macAddress is the natural key — if it already exists the record is updated.
     */
    public record UpsertNetworkDeviceRequest(
            String macAddress,
            String ipAddress,
            String hostname,
            String vendor,
            String switchName,
            String switchPort
    ) {}

    /**
     * Admin request to link / unlink a device to an inventory asset.
     * Pass null linkedAssetId to unlink.
     */
    public record LinkAssetRequest(
            Long linkedAssetId,
            String linkedAssetName
    ) {}

    // ─── Filter params (used by the service / spec) ──────────────────────────

    public record NetworkDeviceFilter(
            String hostname,
            String macAddress,
            String switchName,
            List<String> vendors,
            Boolean isImported
    ) {}
}
