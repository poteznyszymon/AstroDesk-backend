package io.astrodesk.network.controller;

import io.astrodesk.network.dto.NetworkDtos.*;
import io.astrodesk.network.service.NetworkService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Base path: /api/network
 *
 * Endpoints:
 *   GET    /api/network/devices              — list + filter devices (paginated)
 *   GET    /api/network/devices/{id}         — device detail + history
 *   GET    /api/network/devices/{mac}/history — history by MAC (used by the side-panel)
 *   POST   /api/network/devices/upsert       — scanner push (ASSET_ADMIN / HEADADMIN)
 *   PATCH  /api/network/devices/{id}/link    — link/unlink to inventory asset
 *   POST   /api/network/scan                 — trigger manual scan (ASSET_ADMIN / HEADADMIN)
 */
@RestController
@RequestMapping("/api/network")
@RequiredArgsConstructor
public class NetworkController {

    private final NetworkService networkService;

    // ── GET /devices ─────────────────────────────────────────────────────────

    @GetMapping("/devices")
    public ResponseEntity<Page<NetworkItemResponse>> listDevices(
            @RequestParam(required = false) String hostname,
            @RequestParam(required = false) String macAddress,
            @RequestParam(required = false) String switchName,
            @RequestParam(required = false) List<String> vendors,
            @RequestParam(required = false) Boolean isImported,
            @PageableDefault(size = 50, sort = "lastSeenAt") Pageable pageable
    ) {
        NetworkDeviceFilter filter = new NetworkDeviceFilter(
                hostname, macAddress, switchName, vendors, isImported);
        return ResponseEntity.ok(networkService.listDevices(filter, pageable));
    }

    // ── GET /devices/{id} ────────────────────────────────────────────────────

    @GetMapping("/devices/{id}")
    public ResponseEntity<NetworkDeviceDetail> getDevice(@PathVariable Long id) {
        return ResponseEntity.ok(networkService.getDevice(id));
    }

    // ── GET /devices/{mac}/history ───────────────────────────────────────────

    @GetMapping("/devices/{mac}/history")
    public ResponseEntity<List<NetworkHistoryResponse>> getHistory(
            @PathVariable String mac) {
        return ResponseEntity.ok(networkService.getHistory(mac));
    }

    // ── POST /devices/upsert ─────────────────────────────────────────────────

    @PostMapping("/devices/upsert")
    @PreAuthorize("hasAnyRole('ASSET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<NetworkItemResponse> upsertDevice(
            @RequestBody UpsertNetworkDeviceRequest request) {
        return ResponseEntity.ok(networkService.upsertDevice(request));
    }

    // ── PATCH /devices/{id}/link ─────────────────────────────────────────────

    @PatchMapping("/devices/{id}/link")
    @PreAuthorize("hasAnyRole('ASSET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<NetworkItemResponse> linkAsset(
            @PathVariable Long id,
            @RequestBody LinkAssetRequest request) {
        return ResponseEntity.ok(networkService.linkAsset(id, request));
    }

    // ── POST /scan ───────────────────────────────────────────────────────────

    @PostMapping("/scan")
    @PreAuthorize("hasAnyRole('ASSET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<Void> triggerScan() {
        networkService.triggerScan();
        return ResponseEntity.accepted().build();
        
    }
    @PatchMapping("/devices/{id}/hostname")
    @PreAuthorize("hasAnyRole('ASSET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<NetworkItemResponse> updateHostname(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(networkService.updateHostname(id, body.get("hostname")));
    }
}
