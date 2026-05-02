package io.astrodesk.importer;

import java.util.List;

public class ImportValidationResult {

    private final int totalRows;
    private final int validCount;
    private final int errorCount;
    private final List<ImportRowError> errors;

    public ImportValidationResult(int totalRows, int validCount, int errorCount, List<ImportRowError> errors) {
        this.totalRows = totalRows;
        this.validCount = validCount;
        this.errorCount = errorCount;
        this.errors = errors;
    }

    public int getTotalRows() { return totalRows; }
    public int getValidCount() { return validCount; }
    public int getErrorCount() { return errorCount; }
    public List<ImportRowError> getErrors() { return errors; }
}
