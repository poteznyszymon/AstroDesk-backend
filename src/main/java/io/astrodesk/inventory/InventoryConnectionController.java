package io.astrodesk.inventory;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory/{deviceId}/connections")
public class InventoryConnectionController {

    private final InventoryConnectionService connectionService;

    public InventoryConnectionController(InventoryConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @GetMapping
    public List<InventoryConnectionDTO> getConnections(@PathVariable Long deviceId) {
        return connectionService.getConnections(deviceId);
    }

    @PreAuthorize("hasAnyRole('ASSET_ADMIN', 'HEADADMIN')")
    @PostMapping("/{otherDeviceId}")
    public InventoryConnectionDTO addConnection(
            @PathVariable Long deviceId,
            @PathVariable Long otherDeviceId,
            Authentication authentication
    ) {
        return connectionService.addConnection(deviceId, otherDeviceId, authentication);
    }

    @PreAuthorize("hasAnyRole('ASSET_ADMIN', 'HEADADMIN')")
    @DeleteMapping("/{connectionId}")
    public void removeConnection(
            @PathVariable Long deviceId,
            @PathVariable Long connectionId,
            Authentication authentication
    ) {
        connectionService.removeConnection(deviceId, connectionId, authentication);
    }
}
