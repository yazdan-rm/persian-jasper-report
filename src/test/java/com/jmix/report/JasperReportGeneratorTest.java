package com.jmix.report;

import com.jmix.entity.Sale;
import com.jmix.repository.SaleRepository;
import com.jmix.service.SaleReportService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JasperReportGeneratorTest {

    private static final ReportPeriod PERIOD = new ReportPeriod(
            LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

    @Test
    void generatesPersianReportInAllFormats() throws Exception {
        SaleRepository repository = mock(SaleRepository.class);
        when(repository.findBySaleDateBetweenOrderBySaleDateAsc(PERIOD.from(), PERIOD.to()))
                .thenReturn(sampleSales());
        Clock clock = Clock.fixed(Instant.parse("2026-09-30T12:00:00Z"), ZoneOffset.UTC);
        SaleReportService service = new SaleReportService(
                repository, new JasperReportGenerator(), clock, "ریال");

        GeneratedDocument pdf = service.generate(PERIOD, ReportFormat.PDF);
        GeneratedDocument docx = service.generate(PERIOD, ReportFormat.DOCX);
        GeneratedDocument xlsx = service.generate(PERIOD, ReportFormat.XLSX);

        Path output = Path.of("target", "generated-reports");
        Files.createDirectories(output);
        Files.write(output.resolve(pdf.filename()), pdf.content());
        Files.write(output.resolve(docx.filename()), docx.content());
        Files.write(output.resolve(xlsx.filename()), xlsx.content());

        assertThat(new String(pdf.content(), 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        assertThat(new String(pdf.content(), StandardCharsets.ISO_8859_1)).containsIgnoringCase("Yekan");
        assertThat(zipEntryContent(docx.content(), "word/document.xml"))
                .contains("گزارش عملکرد فروش", "فروشگاه سپهر", "هدست بی‌سیم اوربیت", "ریال");
        assertThat(zipEntries(xlsx.content())).contains("xl/workbook.xml", "[Content_Types].xml");
    }

    private List<Sale> sampleSales() {
        return List.of(
                new Sale("INV-1001", LocalDate.of(2026, 9, 12), "فروشگاه سپهر",
                        "هدست بی‌سیم اوربیت", "تجهیزات صوتی", 3, new BigDecimal("18900000")),
                new Sale("INV-1002", LocalDate.of(2026, 9, 18), "شرکت آریا",
                        "نمایشگر همراه ویستا", "نمایشگر", 2, new BigDecimal("32900000")),
                new Sale("INV-1003", LocalDate.of(2026, 9, 24), "استودیو آیونیکس",
                        "صفحه‌کلید کامپکت کانواس", "لوازم جانبی", 5, new BigDecimal("13900000"))
        );
    }

    private List<String> zipEntries(byte[] content) throws Exception {
        var entries = new java.util.ArrayList<String>();
        try (ZipInputStream input = new ZipInputStream(new java.io.ByteArrayInputStream(content))) {
            ZipEntry entry;
            while ((entry = input.getNextEntry()) != null) {
                entries.add(entry.getName());
            }
        }
        return entries;
    }

    private String zipEntryContent(byte[] content, String entryName) throws Exception {
        try (ZipInputStream input = new ZipInputStream(new java.io.ByteArrayInputStream(content))) {
            ZipEntry entry;
            while ((entry = input.getNextEntry()) != null) {
                if (entryName.equals(entry.getName())) {
                    return new String(input.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }
        throw new IllegalArgumentException("ZIP entry not found: " + entryName);
    }
}
