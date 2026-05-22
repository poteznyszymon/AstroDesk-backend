package io.astrodesk.network.scanner;

import java.util.List;

public interface NetworkScannerService {
    void scanNow(List<String> subnets);
}
