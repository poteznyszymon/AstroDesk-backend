package io.astrodesk.network.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "network_history",
        indexes = @Index(name = "idx_network_history_mac", columnList = "mac_address"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NetworkHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private NetworkDevice device;

    /**
     * Denormalized for easy querying by the frontend (filtered by macAddress).
     */
    @Column(name = "mac_address", nullable = false, length = 17)
    private String macAddress;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "switch_name", length = 100)
    private String switchName;

    @Column(name = "switch_port", length = 30)
    private String switchPort;

    @Column(name = "seen_at", nullable = false)
    private Instant seenAt;
}
