package io.astrodesk.export;

import io.astrodesk.history.HistoryEntry;
import io.astrodesk.history.HistoryService;
import io.astrodesk.inventory.Inventory;
import io.astrodesk.inventory.InventoryRepository;
import io.astrodesk.ticket.TicketEntity;
import io.astrodesk.ticket.TicketRepository;
import io.astrodesk.user.UserDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter D_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final String[] TICKET_HEADERS = {
        "ID", "Tytuł", "Opis", "Status", "Priorytet",
        "Autor", "Przypisany do", "Numer seryjny urządzenia",
        "Data utworzenia", "Data aktualizacji"
    };

    private static final String[] INVENTORY_HEADERS = {
        "ID", "Nazwa", "Typ", "Numer seryjny", "Model",
        "Data zakupu", "Cena", "Numer faktury", "Lokalizacja",
        "Przypisano do", "Przypisał", "Data przypisania",
        "Status", "Priorytet", "Autor",
        "Data utworzenia", "Data aktualizacji"
    };

    private static final String[] HISTORY_HEADERS = {
        "ID", "Typ obiektu", "ID obiektu", "Pole",
        "Poprzednia wartość", "Nowa wartość", "Wiadomość",
        "Zmienione przez", "Data zmiany"
    };

    private final TicketRepository ticketRepository;
    private final InventoryRepository inventoryRepository;
    private final HistoryService historyService;

    public ExportService(TicketRepository ticketRepository, InventoryRepository inventoryRepository, HistoryService historyService) {
        this.ticketRepository = ticketRepository;
        this.inventoryRepository = inventoryRepository;
        this.historyService = historyService;
    }

    @Transactional(readOnly = true)
    public byte[] exportTickets(String format) {
        List<TicketEntity> tickets = ticketRepository.findAll();
        return "xlsx".equalsIgnoreCase(format) ? ticketsToXlsx(tickets) : ticketsToCsv(tickets);
    }

    @Transactional(readOnly = true)
    public byte[] exportInventory(String format) {
        List<Inventory> items = inventoryRepository.findAll();
        return "xlsx".equalsIgnoreCase(format) ? inventoryToXlsx(items) : inventoryToCsv(items);
    }

    public byte[] exportHistory(String format) {
        List<HistoryEntry> entries = historyService.getAllHistory();
        return "xlsx".equalsIgnoreCase(format) ? historyToXlsx(entries) : historyToCsv(entries);
    }

    private byte[] ticketsToCsv(List<TicketEntity> tickets) {
        StringBuilder sb = new StringBuilder();
        appendCsvRow(sb, TICKET_HEADERS);
        for (TicketEntity t : tickets) {
            appendCsvRow(sb,
                str(t.getTicketId()),
                t.getTitle(),
                t.getDescription(),
                enumName(t.getStatus()),
                enumName(t.getPriority()),
                fullName(t.getAuthor()),
                fullName(t.getAssignee()),
                nvl(t.getLinkedInventorySerialNumber()),
                fmt(t.getCreatedAt()),
                fmt(t.getUpdatedAt())
            );
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] ticketsToXlsx(List<TicketEntity> tickets) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Tickety");
            writeHeaderRow(sheet, wb, TICKET_HEADERS);
            int rowNum = 1;
            for (TicketEntity t : tickets) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(t.getTicketId());
                row.createCell(1).setCellValue(nvl(t.getTitle()));
                row.createCell(2).setCellValue(nvl(t.getDescription()));
                row.createCell(3).setCellValue(enumName(t.getStatus()));
                row.createCell(4).setCellValue(enumName(t.getPriority()));
                row.createCell(5).setCellValue(fullName(t.getAuthor()));
                row.createCell(6).setCellValue(fullName(t.getAssignee()));
                row.createCell(7).setCellValue(nvl(t.getLinkedInventorySerialNumber()));
                row.createCell(8).setCellValue(fmt(t.getCreatedAt()));
                row.createCell(9).setCellValue(fmt(t.getUpdatedAt()));
            }
            autoSize(sheet, TICKET_HEADERS.length);
            return toBytes(wb);
        } catch (IOException e) {
            throw new RuntimeException("Błąd generowania pliku xlsx dla ticketów", e);
        }
    }

    private byte[] inventoryToCsv(List<Inventory> items) {
        StringBuilder sb = new StringBuilder();
        appendCsvRow(sb, INVENTORY_HEADERS);
        for (Inventory i : items) {
            appendCsvRow(sb,
                str(i.getId()),
                i.getName(),
                enumName(i.getItemType()),
                i.getSerialNumber(),
                nvl(i.getModel()),
                fmtDate(i.getBoughtDate()),
                str(i.getPrice()),
                nvl(i.getInvoiceNumber()),
                nvl(i.getLocation()),
                fullName(i.getAssignedTo()),
                fullName(i.getAssignedBy()),
                fmtDate(i.getAssignedDate()),
                enumName(i.getStatus()),
                enumName(i.getPriority()),
                fullName(i.getAuthor()),
                fmt(i.getCreatedAt()),
                fmt(i.getUpdatedAt())
            );
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] inventoryToXlsx(List<Inventory> items) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Inwentaryzacja");
            writeHeaderRow(sheet, wb, INVENTORY_HEADERS);
            int rowNum = 1;
            for (Inventory i : items) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(i.getId());
                row.createCell(1).setCellValue(nvl(i.getName()));
                row.createCell(2).setCellValue(enumName(i.getItemType()));
                row.createCell(3).setCellValue(nvl(i.getSerialNumber()));
                row.createCell(4).setCellValue(nvl(i.getModel()));
                row.createCell(5).setCellValue(fmtDate(i.getBoughtDate()));
                row.createCell(6).setCellValue(i.getPrice() != null ? i.getPrice() : 0.0);
                row.createCell(7).setCellValue(nvl(i.getInvoiceNumber()));
                row.createCell(8).setCellValue(nvl(i.getLocation()));
                row.createCell(9).setCellValue(fullName(i.getAssignedTo()));
                row.createCell(10).setCellValue(fullName(i.getAssignedBy()));
                row.createCell(11).setCellValue(fmtDate(i.getAssignedDate()));
                row.createCell(12).setCellValue(enumName(i.getStatus()));
                row.createCell(13).setCellValue(enumName(i.getPriority()));
                row.createCell(14).setCellValue(fullName(i.getAuthor()));
                row.createCell(15).setCellValue(fmt(i.getCreatedAt()));
                row.createCell(16).setCellValue(fmt(i.getUpdatedAt()));
            }
            autoSize(sheet, INVENTORY_HEADERS.length);
            return toBytes(wb);
        } catch (IOException e) {
            throw new RuntimeException("Błąd generowania pliku xlsx dla inwentaryzacji", e);
        }
    }

    // --- History ---

    private byte[] historyToCsv(List<HistoryEntry> entries) {
        StringBuilder sb = new StringBuilder();
        appendCsvRow(sb, HISTORY_HEADERS);
        for (HistoryEntry e : entries) {
            appendCsvRow(sb,
                str(e.getId()),
                e.getTargetType() != null ? e.getTargetType().name() : "",
                str(e.getTargetId()),
                nvl(e.getFieldName()),
                nvl(e.getOldValue()),
                nvl(e.getNewValue()),
                nvl(e.getMessage()),
                nvl(e.getChangedBy()),
                fmt(e.getChangedAt())
            );
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private byte[] historyToXlsx(List<HistoryEntry> entries) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Historia zmian");
            writeHeaderRow(sheet, wb, HISTORY_HEADERS);
            int rowNum = 1;
            for (HistoryEntry e : entries) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(e.getId());
                row.createCell(1).setCellValue(e.getTargetType() != null ? e.getTargetType().name() : "");
                row.createCell(2).setCellValue(e.getTargetId() != null ? e.getTargetId() : 0L);
                row.createCell(3).setCellValue(nvl(e.getFieldName()));
                row.createCell(4).setCellValue(nvl(e.getOldValue()));
                row.createCell(5).setCellValue(nvl(e.getNewValue()));
                row.createCell(6).setCellValue(nvl(e.getMessage()));
                row.createCell(7).setCellValue(nvl(e.getChangedBy()));
                row.createCell(8).setCellValue(fmt(e.getChangedAt()));
            }
            autoSize(sheet, HISTORY_HEADERS.length);
            return toBytes(wb);
        } catch (IOException e) {
            throw new RuntimeException("Błąd generowania pliku xlsx dla historii", e);
        }
    }

    private void writeHeaderRow(Sheet sheet, Workbook wb, String[] headers) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private void autoSize(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private byte[] toBytes(Workbook wb) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        return out.toByteArray();
    }

    private void appendCsvRow(StringBuilder sb, String... values) {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(";");
            sb.append(escapeCsv(values[i]));
        }
        sb.append("\n");
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String fullName(UserDTO user) {
        if (user == null) return "";
        return (nvl(user.getFirstName()) + " " + nvl(user.getLastName())).trim();
    }

    private String enumName(Enum<?> e) {
        return e != null ? e.name() : "";
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }

    private String str(Object o) {
        return o != null ? o.toString() : "";
    }

    private String fmt(LocalDateTime dt) {
        return dt != null ? dt.format(DT_FMT) : "";
    }

    private String fmtDate(LocalDate d) {
        return d != null ? d.format(D_FMT) : "";
    }
}
