package io.astrodesk.inventory;

import io.astrodesk.history.HistoryService;
import io.astrodesk.history.HistoryTargetType;
import io.astrodesk.user.DbUserEntity;
import io.astrodesk.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class InventoryConnectionService {

    private final InventoryConnectionRepository connectionRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final HistoryService historyService;

    public InventoryConnectionService(
            InventoryConnectionRepository connectionRepository,
            InventoryRepository inventoryRepository,
            UserRepository userRepository,
            HistoryService historyService
    ) {
        this.connectionRepository = connectionRepository;
        this.inventoryRepository = inventoryRepository;
        this.userRepository = userRepository;
        this.historyService = historyService;
    }

    public List<InventoryConnectionDTO> getConnections(Long deviceId) {
        Inventory device = getDevice(deviceId);
        return connectionRepository.findAllByDevice(device)
                .stream()
                .map(c -> InventoryConnectionDTO.from(c, device))
                .toList();
    }

    public InventoryConnectionDTO addConnection(Long deviceId, Long otherDeviceId, Authentication authentication) {
        if (deviceId.equals(otherDeviceId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Urządzenie nie może być połączone samo ze sobą");
        }

        Inventory deviceA = getDevice(deviceId);
        Inventory deviceB = getDevice(otherDeviceId);

        if (connectionRepository.existsConnection(deviceA, deviceB)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Połączenie między tymi urządzeniami już istnieje");
        }

        DbUserEntity user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        InventoryConnection connection = new InventoryConnection(deviceA, deviceB, user);
        InventoryConnection saved = connectionRepository.save(connection);

        historyService.saveMessage(
                HistoryTargetType.INVENTORY,
                deviceA.getId(),
                "Powiązano z urządzeniem: " + deviceB.getName() + " (" + deviceB.getSerialNumber() + ")",
                user.getUsername()
        );
        historyService.saveMessage(
                HistoryTargetType.INVENTORY,
                deviceB.getId(),
                "Powiązano z urządzeniem: " + deviceA.getName() + " (" + deviceA.getSerialNumber() + ")",
                user.getUsername()
        );

        return InventoryConnectionDTO.from(saved, deviceA);
    }

    public void removeConnection(Long deviceId, Long connectionId, Authentication authentication) {
        Inventory device = getDevice(deviceId);

        InventoryConnection connection = connectionRepository.findByIdAndDevice(connectionId, device)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Połączenie nie zostało znalezione"));

        Inventory other = connection.getDeviceA().getId().equals(deviceId)
                ? connection.getDeviceB()
                : connection.getDeviceA();

        String username = authentication.getName();

        connectionRepository.delete(connection);

        historyService.saveMessage(
                HistoryTargetType.INVENTORY,
                device.getId(),
                "Usunięto powiązanie z urządzeniem: " + other.getName() + " (" + other.getSerialNumber() + ")",
                username
        );
        historyService.saveMessage(
                HistoryTargetType.INVENTORY,
                other.getId(),
                "Usunięto powiązanie z urządzeniem: " + device.getName() + " (" + device.getSerialNumber() + ")",
                username
        );
    }

    private Inventory getDevice(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.INVENTORY_NOT_FOUND));
    }
}
