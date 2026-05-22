package io.astrodesk.network.service;

import io.astrodesk.network.dto.NetworkDtos.AvailableSubnetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Slf4j
@Service
public class NetworkInterfaceService {

    public List<AvailableSubnetResponse> getAvailableSubnets() {
        List<AvailableSubnetResponse> result = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces == null) return result;

            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;

                ni.getInterfaceAddresses().forEach(ia -> {
                    if (!(ia.getAddress() instanceof Inet4Address)) return;
                    String subnet = computeSubnet(ia.getAddress().getAddress(), ia.getNetworkPrefixLength());
                    result.add(new AvailableSubnetResponse(ni.getDisplayName(), subnet));
                });
            }
        } catch (Exception e) {
            log.error("[NetworkInterfaceService] Failed to enumerate interfaces: {}", e.getMessage());
        }
        return result;
    }

    private String computeSubnet(byte[] ip, int prefixLen) {
        int mask = prefixLen == 0 ? 0 : (0xFFFFFFFF << (32 - prefixLen));
        int network = ((ip[0] & 0xFF) << 24 | (ip[1] & 0xFF) << 16 | (ip[2] & 0xFF) << 8 | (ip[3] & 0xFF)) & mask;
        return String.format("%d.%d.%d.%d/%d",
                (network >> 24) & 0xFF,
                (network >> 16) & 0xFF,
                (network >> 8)  & 0xFF,
                 network        & 0xFF,
                prefixLen);
    }
}
