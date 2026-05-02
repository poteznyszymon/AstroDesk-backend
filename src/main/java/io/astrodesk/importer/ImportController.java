package io.astrodesk.importer;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/import")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/inventory/validate")
    @PreAuthorize("hasAnyRole('ASSET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<ImportValidationResult> validateInventory(
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(importService.validateInventory(file));
    }

    @PostMapping("/inventory")
    @PreAuthorize("hasAnyRole('ASSET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<ImportSummary> importInventory(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "skipErrors", defaultValue = "false") boolean skipErrors,
            Authentication auth) throws IOException {
        return ResponseEntity.ok(importService.importInventory(file, skipErrors, auth));
    }

    @PostMapping("/tickets/validate")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<ImportValidationResult> validateTickets(
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(importService.validateTickets(file));
    }

    @PostMapping("/tickets")
    @PreAuthorize("hasAnyRole('TICKET_ADMIN', 'HEADADMIN')")
    public ResponseEntity<ImportSummary> importTickets(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "skipErrors", defaultValue = "false") boolean skipErrors,
            Authentication auth) throws IOException {
        return ResponseEntity.ok(importService.importTickets(file, skipErrors, auth));
    }
}
