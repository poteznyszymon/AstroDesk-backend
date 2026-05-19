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

@RestController
@RequestMapping("/api/network")
@RequiredArgsConstructor
public class NetworkController {

    private final NetworkService networkService;

    @GetMapping("/devices")
    public ResponseEntity<Page<NetworkItemResponse>> listDevices(
            @RequestParam(required = false) String hostname,
            @RequestParam(required = false) String macAddress,
            @RequestParam(required = false) String switchName,
            @RequestParam(required = false) List<String> vendors,
            @PageableDefault(size = 50, sort = "lastSeenAt") Pageable pageable
    ) {
        NetworkDeviceFilter filter = new NetworkDeviceFilter(hostname, macAddress, switchName, vendors);
        return ResponseEntity.ok(networkService.listDevices(filter, pageable));
    }

    @GetMapping("/devices/{id}")
    public ResponseEntity<NetworkDeviceDetail> getDevice(@PathVariable Long id) {
        return ResponseEntity.ok(networkService.getDevice(id));
    }

    @GetMapping("/devices/{mac}/history")
    public ResponseEntity<List<NetworkHistoryResponse>> getHistory(@PathVariable String mac) {
        return ResponseEntity.ok(networkService.getHistory(mac));
    }

    @PostMapping("/devices/upsert")
    @PreAuthorize("hasAnyRole('ASSET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<NetworkItemResponse> upsertDevice(@RequestBody UpsertNetworkDeviceRequest request) {
        return ResponseEntity.ok(networkService.upsertDevice(request));
    }

    @PatchMapping("/devices/{id}/hostname")
    @PreAuthorize("hasAnyRole('ASSET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<NetworkItemResponse> updateHostname(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(networkService.updateHostname(id, body.get("hostname")));
    }

    @PostMapping("/scan")
    @PreAuthorize("hasAnyRole('ASSET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<Void> triggerScan() {
        networkService.triggerScan();
        return ResponseEntity.accepted().build();
    }
}
