package io.astrodesk.inventory;

public class AssignableInventoryDTO {
    private Long id;
    private String name;
    private String serialNumber;
    private InventoryItemType itemType;
    private InventoryStatus status;

    public AssignableInventoryDTO() {}

    public AssignableInventoryDTO(Long id, String name, String serialNumber, InventoryItemType itemType, InventoryStatus status) {
        this.id = id;
        this.name = name;
        this.serialNumber = serialNumber;
        this.itemType = itemType;
        this.status = status;
    }

    public static AssignableInventoryDTO from(Inventory inventory) {
        return new AssignableInventoryDTO(
                inventory.getId(),
                inventory.getName(),
                inventory.getSerialNumber(),
                inventory.getItemType(),
                inventory.getStatus()
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public InventoryItemType getItemType() {
        return itemType;
    }

    public InventoryStatus getStatus() {
        return status;
    }
}
