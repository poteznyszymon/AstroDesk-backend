package io.astrodesk.inventory;

import io.astrodesk.user.UserDTO;

import java.time.LocalDateTime;

public class InventoryConnectionDTO {

    private Long connectionId;
    private Long connectedDeviceId;
    private String connectedDeviceName;
    private String connectedDeviceSerialNumber;
    private InventoryItemType connectedDeviceItemType;
    private InventoryStatus connectedDeviceStatus;
    private UserDTO createdBy;
    private LocalDateTime createdAt;

    public static InventoryConnectionDTO from(InventoryConnection connection, Inventory perspective) {
        Inventory other = connection.getDeviceA().getId().equals(perspective.getId())
                ? connection.getDeviceB()
                : connection.getDeviceA();

        InventoryConnectionDTO dto = new InventoryConnectionDTO();
        dto.connectionId = connection.getId();
        dto.connectedDeviceId = other.getId();
        dto.connectedDeviceName = other.getName();
        dto.connectedDeviceSerialNumber = other.getSerialNumber();
        dto.connectedDeviceItemType = other.getItemType();
        dto.connectedDeviceStatus = other.getStatus();
        dto.createdBy = connection.getCreatedBy();
        dto.createdAt = connection.getCreatedAt();
        return dto;
    }

    public Long getConnectionId() { return connectionId; }
    public Long getConnectedDeviceId() { return connectedDeviceId; }
    public String getConnectedDeviceName() { return connectedDeviceName; }
    public String getConnectedDeviceSerialNumber() { return connectedDeviceSerialNumber; }
    public InventoryItemType getConnectedDeviceItemType() { return connectedDeviceItemType; }
    public InventoryStatus getConnectedDeviceStatus() { return connectedDeviceStatus; }
    public UserDTO getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
