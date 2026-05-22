package io.astrodesk.network.scanner;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class NetworkEnricher {

    private static final List<String> OUI_PATHS = List.of(
        "/opt/homebrew/share/nmap/nmap-mac-prefixes",
        "/usr/local/share/nmap/nmap-mac-prefixes",
        "/usr/share/nmap/nmap-mac-prefixes",
        "C:/Program Files (x86)/Nmap/nmap-mac-prefixes",
        "C:/Program Files/Nmap/nmap-mac-prefixes"
    );

    // ? (192.168.1.1) at aa:bb:cc:dd:ee:ff on eth0 ...
    private static final Pattern ARP_UNIX = Pattern.compile(
        "\\((\\d+\\.\\d+\\.\\d+\\.\\d+)\\)\\s+at\\s+([0-9a-fA-F]{2}:[0-9a-fA-F]{2}:[0-9a-fA-F]{2}:[0-9a-fA-F]{2}:[0-9a-fA-F]{2}:[0-9a-fA-F]{2})"
    );

    // 192.168.1.1    aa-bb-cc-dd-ee-ff    dynamic  (Windows)
    private static final Pattern ARP_WIN = Pattern.compile(
        "(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+([0-9a-fA-F]{2}-[0-9a-fA-F]{2}-[0-9a-fA-F]{2}-[0-9a-fA-F]{2}-[0-9a-fA-F]{2}-[0-9a-fA-F]{2})"
    );

    private final Map<String, String> ouiCache = new HashMap<>();

    @PostConstruct
    void loadOui() {
        for (String path : OUI_PATHS) {
            File f = new File(path);
            if (!f.exists()) continue;
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("#") || line.length() < 7) continue;
                    String prefix = line.substring(0, 6).toUpperCase();
                    String vendor = line.substring(6).trim();
                    ouiCache.put(prefix, vendor);
                }
                log.info("[NetworkEnricher] Loaded {} OUI entries from {}", ouiCache.size(), path);
                return;
            } catch (Exception e) {
                log.warn("[NetworkEnricher] Failed to load OUI file {}: {}", path, e.getMessage());
            }
        }
        log.warn("[NetworkEnricher] No OUI file found — vendor lookup disabled");
    }

    public Map<String, String> getArpTable() {
        // Linux: prefer /proc/net/arp (always available, no external command needed)
        File procArp = new File("/proc/net/arp");
        if (procArp.exists()) {
            return readProcNetArp(procArp);
        }
        return readArpCommand();
    }

    private Map<String, String> readProcNetArp(File procArp) {
        Map<String, String> result = new HashMap<>();
        // Format: IP address  HW type  Flags  HW address           Mask  Device
        //         192.168.1.1 0x1      0x2    aa:bb:cc:dd:ee:ff    *     eth0
        Pattern p = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+\\S+\\s+(0x2)\\s+([0-9a-fA-F:]{17})");
        try (BufferedReader br = new BufferedReader(new FileReader(procArp))) {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.find()) {
                    result.put(m.group(1), m.group(3).toUpperCase());
                }
            }
        } catch (Exception e) {
            log.warn("[NetworkEnricher] Failed to read /proc/net/arp: {}", e.getMessage());
        }
        return result;
    }

    private Map<String, String> readArpCommand() {
        Map<String, String> result = new HashMap<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("arp", "-a");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            String output = new String(p.getInputStream().readAllBytes());
            p.waitFor();

            Matcher m = ARP_UNIX.matcher(output);
            while (m.find()) {
                result.put(m.group(1), m.group(2).toUpperCase());
            }

            if (result.isEmpty()) {
                m = ARP_WIN.matcher(output);
                while (m.find()) {
                    result.put(m.group(1), m.group(2).replace("-", ":").toUpperCase());
                }
            }
        } catch (Exception e) {
            log.warn("[NetworkEnricher] arp command failed: {}", e.getMessage());
        }
        return result;
    }

    public String getMacForLocalIp(String ipAddress) {
        try {
            InetAddress addr = InetAddress.getByName(ipAddress);
            NetworkInterface ni = NetworkInterface.getByInetAddress(addr);
            if (ni == null) return null;
            byte[] mac = ni.getHardwareAddress();
            if (mac == null) return null;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                if (i > 0) sb.append(":");
                sb.append(String.format("%02X", mac[i]));
            }
            return sb.toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    public String lookupVendor(String mac) {
        if (mac == null || mac.length() < 8 || ouiCache.isEmpty()) return null;
        String prefix = mac.replace(":", "").substring(0, 6).toUpperCase();
        return ouiCache.get(prefix);
    }
}
