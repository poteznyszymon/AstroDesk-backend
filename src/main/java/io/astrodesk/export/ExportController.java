package io.astrodesk.export;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/export")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/tickets")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<byte[]> exportTickets(@RequestParam(defaultValue = "csv") String format) {
        var data = exportService.exportTickets(format);
        return fileResponse(data, "tickets", format);
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasAnyRole('ASSET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<byte[]> exportInventory(@RequestParam(defaultValue = "csv") String format) {
        var data = exportService.exportInventory(format);
        return fileResponse(data, "inventory", format);
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'ASSET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<byte[]> exportHistory(@RequestParam(defaultValue = "csv") String format) {
        var data = exportService.exportHistory(format);
        return fileResponse(data, "history", format);
    }

    private ResponseEntity<byte[]> fileResponse(byte[] data, String name, String format) {
        var contentType = "xlsx".equalsIgnoreCase(format)
                ? MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                : MediaType.parseMediaType("text/csv; charset=UTF-8");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "." + format + "\"")
                .contentType(contentType)
                .body(data);
    }
}
