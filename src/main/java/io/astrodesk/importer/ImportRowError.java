package io.astrodesk.importer;

public class ImportRowError {

    private final int row;
    private final String field;
    private final String message;

    public ImportRowError(int row, String field, String message) {
        this.row = row;
        this.field = field;
        this.message = message;
    }

    public int getRow() { return row; }
    public String getField() { return field; }
    public String getMessage() { return message; }
}
