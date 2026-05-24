package io.astrodesk.network.service;

import io.astrodesk.network.dto.NetworkDtos.*;
import io.astrodesk.network.entity.NetworkDevice;
import io.astrodesk.network.entity.NetworkHistory;
import io.astrodesk.network.mapper.NetworkMapper;
import io.astrodesk.network.repository.NetworkDeviceRepository;
import io.astrodesk.network.repository.NetworkDeviceSpecs;
import io.astrodesk.network.repository.NetworkHistoryRepository;
import io.astrodesk.network.scanner.NetworkScannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class NetworkService {

    private final NetworkDeviceRepository deviceRepo;
    private final NetworkHistoryRepository historyRepo;
    private final NetworkMapper mapper;

    @Nullable
    @Autowired(required = false)
    private NetworkScannerService scanner;

    @Transactional(readOnly = true)
    public Page<NetworkItemResponse> listDevices(NetworkDeviceFilter filter, Pageable pageable) {
        return deviceRepo
                .findAll(NetworkDeviceSpecs.fromFilter(filter), pageable)
                .map(mapper::toItemResponse);
    }

    @Transactional(readOnly = true)
    public NetworkDeviceDetail getDevice(Long id) {
        NetworkDevice device = deviceRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Device not found: " + id));

        List<NetworkHistoryResponse> history =
                historyRepo.findByMacAddressOrderBySeenAtDesc(device.getMacAddress())
                           .stream()
                           .map(mapper::toHistoryResponse)
                           .toList();

        return new NetworkDeviceDetail(mapper.toItemResponse(device), history);
    }

    @Transactional(readOnly = true)
    public List<NetworkHistoryResponse> getHistory(String macAddress) {
        return historyRepo.findByMacAddressOrderBySeenAtDesc(macAddress)
                          .stream()
                          .map(mapper::toHistoryResponse)
                          .toList();
    }

    @Transactional
    public NetworkItemResponse upsertDevice(UpsertNetworkDeviceRequest req) {
        // Scal stary placeholder "IP:<ip>" gdy teraz znamy prawdziwy MAC dla tego samego IP.
        if (req.macAddress() != null
                && !req.macAddress().startsWith("IP:")
                && req.ipAddress() != null) {
            String placeholderMac = "IP:" + req.ipAddress();
            deviceRepo.findByMacAddress(placeholderMac).ifPresent(placeholder -> {
                historyRepo.deleteAll(
                        historyRepo.findByMacAddressOrderBySeenAtDesc(placeholderMac));
                deviceRepo.delete(placeholder);
            });
        }

        NetworkDevice device = deviceRepo.findByMacAddress(req.macAddress())
                .orElseGet(() -> NetworkDevice.builder()
                        .macAddress(req.macAddress())
                        .hostnameCustomized(false)
                        .build());

        boolean changed = !Objects.equals(req.ipAddress(), device.getIpAddress())
                || !Objects.equals(req.switchName(), device.getSwitchName())
                || !Objects.equals(req.switchPort(), device.getSwitchPort());

        device.setIpAddress(req.ipAddress());
        if (!device.isHostnameCustomized()) {
            device.setHostname(req.hostname());
        }
        device.setVendor(req.vendor());
        device.setSwitchName(req.switchName());
        device.setSwitchPort(req.switchPort());
        device.setOpenPorts(req.openPorts());
        device.setLastSeenAt(Instant.now());

        device = deviceRepo.save(device);

        if (changed) {
            NetworkHistory entry = NetworkHistory.builder()
                    .device(device)
                    .macAddress(device.getMacAddress())
                    .ipAddress(req.ipAddress())
                    .switchName(req.switchName())
                    .switchPort(req.switchPort())
                    .seenAt(device.getLastSeenAt())
                    .build();
            historyRepo.save(entry);
        }

        return mapper.toItemResponse(device);
    }

    @Transactional
    public NetworkItemResponse updateHostname(Long id, String hostname) {
        NetworkDevice device = deviceRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Device not found: " + id));
        device.setHostname(hostname);
        device.setHostnameCustomized(hostname != null);
        return mapper.toItemResponse(deviceRepo.save(device));
    }

    public void triggerScan(List<String> subnets) {
        if (scanner == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Scanner not available in current profile");
        }
        CompletableFuture.runAsync(() -> scanner.scanNow(subnets));
    }
}
