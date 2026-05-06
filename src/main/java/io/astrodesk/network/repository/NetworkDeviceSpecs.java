package io.astrodesk.network.repository;

import io.astrodesk.network.dto.NetworkDtos.NetworkDeviceFilter;
import io.astrodesk.network.entity.NetworkDevice;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class NetworkDeviceSpecs {

    private NetworkDeviceSpecs() {}

    public static Specification<NetworkDevice> fromFilter(NetworkDeviceFilter f) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (f.hostname() != null && !f.hostname().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("hostname")),
                        "%" + f.hostname().toLowerCase() + "%"));
            }
            if (f.macAddress() != null && !f.macAddress().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("macAddress")),
                        "%" + f.macAddress().toLowerCase() + "%"));
            }
            if (f.switchName() != null && !f.switchName().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("switchName")),
                        "%" + f.switchName().toLowerCase() + "%"));
            }
            if (f.vendors() != null && !f.vendors().isEmpty()) {
                predicates.add(root.get("vendor").in(f.vendors()));
            }
            if (f.isImported() != null) {
                predicates.add(cb.equal(root.get("imported"), f.isImported()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
