package io.astrodesk.importer;

import java.util.List;

public class ImportSummary {

    private final int imported;
    private final int skipped;
    private final List<ImportRowError> errors;

    public ImportSummary(int imported, int skipped, List<ImportRowError> errors) {
        this.imported = imported;
        this.skipped = skipped;
        this.errors = errors;
    }

    public int getImported() { return imported; }
    public int getSkipped() { return skipped; }
    public List<ImportRowError> getErrors() { return errors; }
}
