package io.astrodesk.network.scanner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class NmapRunner {

    // Ścieżka do nmap.exe — zmień jeśli masz inną instalację
    @Value("${network.scanner.nmap-path:nmap}")
    private String nmapPath;

    // Porty do skanowania
    @Value("${network.scanner.ports:22,80,443,3389,8080,445,139,21,25,53}")
    private String ports;

    /**
     * Skanuje podaną podsieć i zwraca listę znalezionych hostów.
     * Wymaga uruchomienia jako Administrator (żeby nmap mógł odczytać MAC adresy).
     */
    public List<NmapScanResult> scan(String subnet) throws Exception {
        // -sn  = ping scan (odkryj hosty)
        // -T4  = szybkie timeouty
        // --open = pokaż tylko otwarte porty
        // -oX - = wyślij XML na stdout
        // -p    = porty do skanowania
        String args = String.format(
            "-T4 --host-timeout 10s --open -oX - -R -p %s %s",
            ports, subnet
        );

        log.info("[NmapRunner] Running: {} {}", nmapPath, args);

        ProcessBuilder pb = new ProcessBuilder();
        pb.command(buildCommand(args));
        pb.redirectErrorStream(false);

        Process process = pb.start();

        // Czytamy stdout (XML) i stderr osobno
        byte[] xmlOutput = process.getInputStream().readAllBytes();
        byte[] errOutput = process.getErrorStream().readAllBytes();

        int exitCode = process.waitFor();

        if (errOutput.length > 0) {
            log.debug("[NmapRunner] stderr: {}", new String(errOutput));
        }
        if (exitCode != 0) {
            throw new RuntimeException("nmap exited with code " + exitCode + ": " + new String(errOutput));
        }

        return parseXml(xmlOutput);
    }

    // ── XML parsing ──────────────────────────────────────────────────────────

    private List<NmapScanResult> parseXml(byte[] xml) throws Exception {
        List<NmapScanResult> results = new ArrayList<>();

        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml));

        NodeList hosts = doc.getElementsByTagName("host");

        for (int i = 0; i < hosts.getLength(); i++) {
            Element host = (Element) hosts.item(i);

            // Tylko hosty które odpowiedziały
            Element status = (Element) host.getElementsByTagName("status").item(0);
            if (status == null || !"up".equals(status.getAttribute("state"))) continue;

            String ipAddress  = null;
            String macAddress = null;
            String vendor     = null;

            // Adresy IP i MAC
            NodeList addresses = host.getElementsByTagName("address");
            for (int j = 0; j < addresses.getLength(); j++) {
                Element addr = (Element) addresses.item(j);
                switch (addr.getAttribute("addrtype")) {
                    case "ipv4" -> ipAddress  = addr.getAttribute("addr");
                    case "mac"  -> {
                        macAddress = addr.getAttribute("addr");
                        vendor     = addr.getAttribute("vendor");
                        if (vendor.isBlank()) vendor = null;
                    }
                }
            }

            if (ipAddress == null) continue;
            if (macAddress == null) macAddress = "IP:" + ipAddress; // fallback bez uprawnień admina

            // Hostname
            String hostname = null;
            NodeList hostnames = host.getElementsByTagName("hostname");
            if (hostnames.getLength() > 0) {
                String name = ((Element) hostnames.item(0)).getAttribute("name");
                if (!name.isBlank()) hostname = name;
            }

            // Otwarte porty
            List<NmapScanResult.OpenPort> openPorts = new ArrayList<>();
            NodeList ports = host.getElementsByTagName("port");
            for (int k = 0; k < ports.getLength(); k++) {
                Element port    = (Element) ports.item(k);
                Element portState = (Element) port.getElementsByTagName("state").item(0);
                if (portState == null || !"open".equals(portState.getAttribute("state"))) continue;

                int    portNumber = Integer.parseInt(port.getAttribute("portid"));
                String service    = "unknown";
                Element svc = (Element) port.getElementsByTagName("service").item(0);
                if (svc != null && !svc.getAttribute("name").isBlank()) {
                    service = svc.getAttribute("name");
                }
                openPorts.add(new NmapScanResult.OpenPort(portNumber, service));
            }

            results.add(new NmapScanResult(ipAddress, macAddress, hostname, vendor, openPorts));
            log.debug("[NmapRunner] Found: {} ({}) vendor={} ports={}", ipAddress, macAddress, vendor, openPorts.size());
        }

        log.info("[NmapRunner] Parsed {} hosts from XML", results.size());
        return results;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private List<String> buildCommand(String args) {
        List<String> cmd = new ArrayList<>();
        cmd.add(nmapPath);
        for (String arg : args.split(" ")) {
            if (!arg.isBlank()) cmd.add(arg);
        }
        return cmd;
    }
}
