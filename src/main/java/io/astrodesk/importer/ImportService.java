package io.astrodesk.importer;

import io.astrodesk.history.HistoryService;
import io.astrodesk.history.HistoryTargetType;
import io.astrodesk.inventory.*;
import io.astrodesk.ticket.*;
import io.astrodesk.user.DbUserEntity;
import io.astrodesk.user.UserRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ImportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final InventoryRepository inventoryRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final HistoryService historyService;

    public ImportService(
            InventoryRepository inventoryRepository,
            TicketRepository ticketRepository,
            UserRepository userRepository,
            HistoryService historyService
    ) {
        this.inventoryRepository = inventoryRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.historyService = historyService;
    }


    public ImportValidationResult validateInventory(MultipartFile file) throws IOException {
        List<Map<String, String>> rows = parseFile(file);
        Set<String> seenSerials = new HashSet<>();
        List<ImportRowError> errors = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            errors.addAll(validateInventoryRow(rows.get(i), i + 2, seenSerials));
        }

        Set<Integer> errorRows = errors.stream().map(ImportRowError::getRow).collect(Collectors.toSet());
        int validCount = rows.size() - errorRows.size();
        return new ImportValidationResult(rows.size(), validCount, errorRows.size(), errors);
    }

    @Transactional
    public ImportSummary importInventory(MultipartFile file, boolean skipErrors, Authentication auth) throws IOException {
        DbUserEntity author = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Map<String, String>> rows = parseFile(file);
        Set<String> seenSerials = new HashSet<>();
        List<ImportRowError> allErrors = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            allErrors.addAll(validateInventoryRow(rows.get(i), i + 2, seenSerials));
        }

        if (!allErrors.isEmpty() && !skipErrors) {
            return new ImportSummary(0, 0, allErrors);
        }

        Set<Integer> errorRows = allErrors.stream().map(ImportRowError::getRow).collect(Collectors.toSet());
        int imported = 0;
        int skipped = 0;

        for (int i = 0; i < rows.size(); i++) {
            int rowNum = i + 2;
            Map<String, String> row = rows.get(i);

            if (errorRows.contains(rowNum)) {
                skipped++;
                continue;
            }

            Inventory inv = buildInventory(row, author);
            inventoryRepository.save(inv);
            historyService.saveMessage(HistoryTargetType.INVENTORY, inv.getId(), "Zaimportowano urządzenie", auth.getName());

            String assignedToUsername = get(row, "Przypisano do");
            String assignedByUsername = get(row, "Przypisał");
            if (!isEmpty(assignedToUsername) && !isEmpty(assignedByUsername)) {
                DbUserEntity assignedTo = findUser(assignedToUsername).orElseThrow();
                DbUserEntity assignedBy = findUser(assignedByUsername).orElseThrow();
                inv.assign(assignedTo, assignedBy);
                inventoryRepository.save(inv);
                historyService.saveMessage(HistoryTargetType.INVENTORY, inv.getId(),
                        "Przypisano urządzenie do: " + assignedToUsername, auth.getName());
            }

            imported++;
        }

        return new ImportSummary(imported, skipped, allErrors);
    }


    public ImportValidationResult validateTickets(MultipartFile file) throws IOException {
        List<Map<String, String>> rows = parseFile(file);
        List<ImportRowError> errors = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            errors.addAll(validateTicketRow(rows.get(i), i + 2));
        }

        Set<Integer> errorRows = errors.stream().map(ImportRowError::getRow).collect(Collectors.toSet());
        int validCount = rows.size() - errorRows.size();
        return new ImportValidationResult(rows.size(), validCount, errorRows.size(), errors);
    }

    @Transactional
    public ImportSummary importTickets(MultipartFile file, boolean skipErrors, Authentication auth) throws IOException {
        DbUserEntity author = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Map<String, String>> rows = parseFile(file);
        List<ImportRowError> allErrors = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            allErrors.addAll(validateTicketRow(rows.get(i), i + 2));
        }

        if (!allErrors.isEmpty() && !skipErrors) {
            return new ImportSummary(0, 0, allErrors);
        }

        Set<Integer> errorRows = allErrors.stream().map(ImportRowError::getRow).collect(Collectors.toSet());
        int imported = 0;
        int skipped = 0;

        for (int i = 0; i < rows.size(); i++) {
            int rowNum = i + 2;
            Map<String, String> row = rows.get(i);

            if (errorRows.contains(rowNum)) {
                skipped++;
                continue;
            }

            TicketPriority priority = TicketPriority.valueOf(get(row, "Priorytet").toUpperCase());

            DbUserEntity assignedTo = null;
            String assignedToUsername = get(row, "Przypisany do");
            if (!isEmpty(assignedToUsername)) {
                assignedTo = findUser(assignedToUsername).orElse(null);
            }

            Inventory linkedInventory = null;
            String serialNumber = get(row, "Numer seryjny urządzenia");
            if (!isEmpty(serialNumber)) {
                linkedInventory = inventoryRepository.findBySerialNumber(serialNumber).orElse(null);
            }

            TicketEntity ticket = new TicketEntity(
                    get(row, "Tytuł"),
                    get(row, "Opis"),
                    priority,
                    author,
                    assignedTo,
                    linkedInventory
            );
            ticketRepository.save(ticket);

            historyService.saveMessage(HistoryTargetType.TICKET, ticket.getTicketId(),
                    "Zaimportowano ticket", auth.getName());

            imported++;
        }

        return new ImportSummary(imported, skipped, allErrors);
    }


    private List<ImportRowError> validateInventoryRow(Map<String, String> row, int rowNum, Set<String> seenSerials) {
        List<ImportRowError> errors = new ArrayList<>();

        if (isEmpty(get(row, "Nazwa"))) {
            errors.add(new ImportRowError(rowNum, "Nazwa", "Pole jest wymagane"));
        }

        String serialNumber = get(row, "Numer seryjny");
        if (isEmpty(serialNumber)) {
            errors.add(new ImportRowError(rowNum, "Numer seryjny", "Pole jest wymagane"));
        } else if (seenSerials.contains(serialNumber)) {
            errors.add(new ImportRowError(rowNum, "Numer seryjny", "Duplikat w pliku: \"" + serialNumber + "\""));
        } else {
            seenSerials.add(serialNumber);
            if (inventoryRepository.findBySerialNumber(serialNumber).isPresent()) {
                errors.add(new ImportRowError(rowNum, "Numer seryjny", "Już istnieje w bazie: \"" + serialNumber + "\""));
            }
        }

        String typ = get(row, "Typ");
        if (isEmpty(typ)) {
            errors.add(new ImportRowError(rowNum, "Typ", "Pole jest wymagane"));
        } else {
            try { InventoryItemType.valueOf(typ.toUpperCase()); }
            catch (IllegalArgumentException e) {
                errors.add(new ImportRowError(rowNum, "Typ", "Nieprawidłowa wartość: \"" + typ + "\""));
            }
        }

        String cena = get(row, "Cena");
        if (!isEmpty(cena)) {
            try { Double.parseDouble(cena.replace(",", ".")); }
            catch (NumberFormatException e) {
                errors.add(new ImportRowError(rowNum, "Cena", "Musi być liczbą: \"" + cena + "\""));
            }
        }

        String dataZakupu = get(row, "Data zakupu");
        if (!isEmpty(dataZakupu)) {
            try { LocalDate.parse(dataZakupu, DATE_FMT); }
            catch (DateTimeParseException e) {
                errors.add(new ImportRowError(rowNum, "Data zakupu", "Nieprawidłowy format daty (oczekiwano dd.MM.yyyy): \"" + dataZakupu + "\""));
            }
        }

        String assignedTo = get(row, "Przypisano do");
        if (!isEmpty(assignedTo) && findUser(assignedTo).isEmpty()) {
            errors.add(new ImportRowError(rowNum, "Przypisano do", "Użytkownik nie istnieje: \"" + assignedTo + "\""));
        }

        String assignedBy = get(row, "Przypisał");
        if (!isEmpty(assignedBy) && findUser(assignedBy).isEmpty()) {
            errors.add(new ImportRowError(rowNum, "Przypisał", "Użytkownik nie istnieje: \"" + assignedBy + "\""));
        }

        String status = get(row, "Status");
        if (!isEmpty(status)) {
            try { InventoryStatus.valueOf(status.toUpperCase()); }
            catch (IllegalArgumentException e) {
                errors.add(new ImportRowError(rowNum, "Status", "Nieprawidłowa wartość: \"" + status + "\""));
            }
        }

        String priority = get(row, "Priorytet");
        if (!isEmpty(priority)) {
            try { InventoryPriority.valueOf(priority.toUpperCase()); }
            catch (IllegalArgumentException e) {
                errors.add(new ImportRowError(rowNum, "Priorytet", "Nieprawidłowa wartość: \"" + priority + "\""));
            }
        }

        return errors;
    }

    private List<ImportRowError> validateTicketRow(Map<String, String> row, int rowNum) {
        List<ImportRowError> errors = new ArrayList<>();

        if (isEmpty(get(row, "Tytuł"))) {
            errors.add(new ImportRowError(rowNum, "Tytuł", "Pole jest wymagane"));
        }
        if (isEmpty(get(row, "Opis"))) {
            errors.add(new ImportRowError(rowNum, "Opis", "Pole jest wymagane"));
        }

        String priority = get(row, "Priorytet");
        if (isEmpty(priority)) {
            errors.add(new ImportRowError(rowNum, "Priorytet", "Pole jest wymagane"));
        } else {
            try { TicketPriority.valueOf(priority.toUpperCase()); }
            catch (IllegalArgumentException e) {
                errors.add(new ImportRowError(rowNum, "Priorytet", "Nieprawidłowa wartość: \"" + priority + "\""));
            }
        }

        String assignedTo = get(row, "Przypisany do");
        if (!isEmpty(assignedTo) && findUser(assignedTo).isEmpty()) {
            errors.add(new ImportRowError(rowNum, "Przypisany do", "Użytkownik nie istnieje: \"" + assignedTo + "\""));
        }

        String serialNumber = get(row, "Numer seryjny urządzenia");
        if (!isEmpty(serialNumber) && inventoryRepository.findBySerialNumber(serialNumber).isEmpty()) {
            errors.add(new ImportRowError(rowNum, "Numer seryjny urządzenia",
                    "Urządzenie o numerze seryjnym nie istnieje w bazie: \"" + serialNumber + "\""));
        }

        return errors;
    }


    private Inventory buildInventory(Map<String, String> row, DbUserEntity author) {
        InventoryItemType itemType = InventoryItemType.valueOf(get(row, "Typ").toUpperCase());

        LocalDate boughtDate = null;
        String boughtDateStr = get(row, "Data zakupu");
        if (!isEmpty(boughtDateStr)) {
            boughtDate = LocalDate.parse(boughtDateStr, DATE_FMT);
        }

        Double price = null;
        String priceStr = get(row, "Cena");
        if (!isEmpty(priceStr)) {
            price = Double.parseDouble(priceStr.replace(",", "."));
        }

        InventoryPriority priority = null;
        String priorityStr = get(row, "Priorytet");
        if (!isEmpty(priorityStr)) {
            priority = InventoryPriority.valueOf(priorityStr.toUpperCase());
        }

        Inventory inv = new Inventory(
                get(row, "Nazwa"),
                itemType,
                get(row, "Numer seryjny"),
                emptyToNull(get(row, "Model")),
                boughtDate,
                price,
                emptyToNull(get(row, "Numer faktury")),
                emptyToNull(get(row, "Lokalizacja")),
                priority,
                author
        );

        // Set status only when item is not being assigned (assign() sets WYDANE itself)
        String assignedToUsername = get(row, "Przypisano do");
        if (isEmpty(assignedToUsername)) {
            String statusStr = get(row, "Status");
            if (!isEmpty(statusStr)) {
                inv.setStatus(InventoryStatus.valueOf(statusStr.toUpperCase()));
            }
        }

        return inv;
    }


    private List<Map<String, String>> parseFile(MultipartFile file) throws IOException {
        String filename = Objects.requireNonNullElse(file.getOriginalFilename(), "").toLowerCase();
        return filename.endsWith(".xlsx") ? parseXlsx(file) : parseCsv(file);
    }

    private List<Map<String, String>> parseXlsx(MultipartFile file) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return rows;

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cellToString(cell));
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    rowMap.put(headers.get(j), cellToString(cell));
                }
                rows.add(rowMap);
            }
        }
        return rows;
    }

    private List<Map<String, String>> parseCsv(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        String firstLine = new String(bytes, StandardCharsets.UTF_8).lines().findFirst().orElse("");
        char delimiter = firstLine.contains(";") ? ';' : ',';

        List<Map<String, String>> rows = new ArrayList<>();
        try (Reader reader = new InputStreamReader(new java.io.ByteArrayInputStream(bytes), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setDelimiter(delimiter)
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {
            for (CSVRecord record : parser) {
                rows.add(new LinkedHashMap<>(record.toMap()));
            }
        }
        return rows;
    }

    private String cellToString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().format(DATE_FMT);
                }
                double val = cell.getNumericCellValue();
                yield val == Math.floor(val) ? String.valueOf((long) val) : String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }


    private Optional<DbUserEntity> findUser(String value) {
        if (isEmpty(value)) return Optional.empty();
        Optional<DbUserEntity> byUsername = userRepository.findByUsername(value);
        if (byUsername.isPresent()) return byUsername;
        String[] parts = value.trim().split(" ", 2);
        if (parts.length == 2) {
            return userRepository.findByFirstNameAndLastName(parts[0], parts[1]);
        }
        return Optional.empty();
    }

    private String get(Map<String, String> row, String key) {
        return row.getOrDefault(key, "").trim();
    }

    private boolean isEmpty(String s) {
        return s == null || s.isBlank();
    }

    private String emptyToNull(String s) {
        return isEmpty(s) ? null : s;
    }
}
