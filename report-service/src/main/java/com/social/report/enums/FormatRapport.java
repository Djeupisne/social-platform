package com.social.report.enums;

public enum FormatRapport {
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"),
    CSV("text/csv", ".csv"),
    PDF("application/pdf", ".pdf");

    private final String mimeType;
    private final String extension;

    FormatRapport(String mimeType, String extension) {
        this.mimeType = mimeType;
        this.extension = extension;
    }

    public String getMimeType() { return mimeType; }
    public String getExtension() { return extension; }
}