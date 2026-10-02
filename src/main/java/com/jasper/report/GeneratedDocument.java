package com.jasper.report;

import org.springframework.http.MediaType;

public record GeneratedDocument(byte[] content, String filename, MediaType mediaType) {
}
