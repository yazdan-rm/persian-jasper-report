package com.jasper.report;

import com.jasper.exception.InvalidReportRequestException;
import org.springframework.http.MediaType;

import java.util.Locale;

public enum ReportFormat {
    PDF("pdf", MediaType.APPLICATION_PDF),
    DOCX("docx", MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
    XLSX("xlsx", MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

    private final String extension;
    private final MediaType mediaType;

    ReportFormat(String extension, MediaType mediaType) {
        this.extension = extension;
        this.mediaType = mediaType;
    }

    public String extension() {
        return extension;
    }

    public MediaType mediaType() {
        return mediaType;
    }

    public static ReportFormat parse(String value) {
        if (value != null) {
            for (ReportFormat format : values()) {
                if (format.extension.equals(value.toLowerCase(Locale.ROOT))) {
                    return format;
                }
            }
        }
        throw new InvalidReportRequestException("Unsupported report format. Allowed formats: pdf, docx, xlsx.");
    }
}
