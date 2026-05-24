package io.astrodesk.network.scanner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class NmapRunner {

    @Value("${network.scanner.nmap-path:nmap}")
    private String nmapPath;

    @Value("${network.scanner.ports:21,22,23,53,80,135,139,443,445,554,631,3389,5000,8009,8080,8443,9100,32400}")
    private String ports;

    @Value("${network.scanner.scan-timeout-seconds:120}")
    private int scanTimeoutSeconds;

    private final boolean needsSudo = !"root".equals(System.getProperty("user.name"));

    public List<DeviceScanResult> scan(String cidr) {
        log.info("[Nmap] Starting scan of {}", cidr);

        List<String> cmd = new ArrayList<>();
        if (needsSudo) cmd.add("sudo");
        cmd.addAll(List.of(nmapPath, "-sS", "-p", ports, "--script", "nbstat", "-oX", "-", cidr));
        log.debug("[Nmap] Command: {}", String.join(" ", cmd));

        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(false);
            Process process = pb.start();

            CompletableFuture<String> stdoutFuture = CompletableFuture.supplyAsync(() -> {
                try { return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8); }
                catch (Exception e) { return ""; }
            });
            CompletableFuture<String> stderrFuture = CompletableFuture.supplyAsync(() -> {
                try { return new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8); }
                catch (Exception e) { return ""; }
            });

            boolean finished = process.waitFor(scanTimeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.error("[Nmap] Scan timed out after {}s for {}", scanTimeoutSeconds, cidr);
                return List.of();
            }

            String xml = stdoutFuture.join();
            String stderr = stderrFuture.join();

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("[Nmap] Exit code {} for {}. stderr: {}", exitCode, cidr, stderr.trim());
                return List.of();
            }

            List<DeviceScanResult> results = parseXml(xml);
            log.info("[Nmap] Found {} hosts in {}", results.size(), cidr);
            return results;

        } catch (Exception e) {
            log.error("[Nmap] Failed to run scan for {}: {}", cidr, e.getMessage(), e);
            return List.of();
        }
    }

    private List<DeviceScanResult> parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        Document doc = factory.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        List<DeviceScanResult> results = new ArrayList<>();
        NodeList hosts = doc.getElementsByTagName("host");

        for (int i = 0; i < hosts.getLength(); i++) {
            Element host = (Element) hosts.item(i);

            String status = getChildText(host, "status", "state");
            if (!"up".equals(status)) continue;

            String ip = null;
            String mac = null;
            String vendor = null;

            NodeList addresses = host.getElementsByTagName("address");
            for (int j = 0; j < addresses.getLength(); j++) {
                Element addr = (Element) addresses.item(j);
                String type = addr.getAttribute("addrtype");
                if ("ipv4".equals(type)) {
                    ip = addr.getAttribute("addr");
                } else if ("mac".equals(type)) {
                    mac = addr.getAttribute("addr").toUpperCase();
                    String v = addr.getAttribute("vendor");
                    if (v != null && !v.isBlank()) vendor = v;
                }
            }

            if (ip == null) continue;

            String hostname = null;
            NodeList hostnames = host.getElementsByTagName("hostname");
            for (int j = 0; j < hostnames.getLength(); j++) {
                Element hn = (Element) hostnames.item(j);
                String name = hn.getAttribute("name");
                if (name != null && !name.isBlank()) {
                    hostname = name;
                    break;
                }
            }

            if (hostname == null) {
                hostname = parseNbstatName(host);
            }

            List<DeviceScanResult.OpenPort> openPorts = new ArrayList<>();
            NodeList portsNodes = host.getElementsByTagName("port");
            for (int j = 0; j < portsNodes.getLength(); j++) {
                Element port = (Element) portsNodes.item(j);
                Element state = getChildElement(port, "state");
                if (state == null || !"open".equals(state.getAttribute("state"))) continue;

                int portNum = Integer.parseInt(port.getAttribute("portid"));
                Element service = getChildElement(port, "service");
                String serviceName = service != null ? service.getAttribute("name") : "unknown";
                openPorts.add(new DeviceScanResult.OpenPort(portNum, serviceName));
            }

            results.add(new DeviceScanResult(ip, mac, hostname, vendor, openPorts));
        }

        return results;
    }

    private String parseNbstatName(Element host) {
        NodeList scripts = host.getElementsByTagName("script");
        for (int i = 0; i < scripts.getLength(); i++) {
            Element script = (Element) scripts.item(i);
            if (!"nbstat".equals(script.getAttribute("id"))) continue;

            NodeList elems = script.getElementsByTagName("elem");
            for (int j = 0; j < elems.getLength(); j++) {
                Element elem = (Element) elems.item(j);
                if ("server_name".equals(elem.getAttribute("key"))) {
                    String name = elem.getTextContent().trim();
                    if (!name.isBlank()) return name;
                }
            }
        }
        return null;
    }

    private String getChildText(Element parent, String tagName, String attribute) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) return null;
        return ((Element) nodes.item(0)).getAttribute(attribute);
    }

    private Element getChildElement(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? (Element) nodes.item(0) : null;
    }
}
