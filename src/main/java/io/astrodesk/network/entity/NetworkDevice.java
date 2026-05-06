package io.astrodesk.network.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "network_devices",
        uniqueConstraints = @UniqueConstraint(columnNames = "mac_address"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NetworkDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mac_address", nullable = false, unique = true, length = 17)
    private String macAddress;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "hostname")
    private String hostname;

    @Column(name = "hostname_customized", nullable = false)
    private boolean hostnameCustomized = false;

    @Column(name = "vendor", length = 100)
    private String vendor;

    @Column(name = "switch_name", length = 100)
    private String switchName;

    @Column(name = "switch_port", length = 30)
    private String switchPort;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    @Column(name = "is_imported", nullable = false)
    private boolean imported;

    @Column(name = "linked_asset_id")
    private Long linkedAssetId;

    @Column(name = "linked_asset_name")
    private String linkedAssetName;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true,
               fetch = FetchType.LAZY)
    @OrderBy("seenAt DESC")
    private List<NetworkHistory> history;
}
