package com.jasper.controller;

import com.jasper.report.GeneratedDocument;
import com.jasper.report.ReportFormat;
import com.jasper.report.ReportPeriod;
import com.jasper.service.SaleReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class SaleReportController {

    private final SaleReportService reportService;
    private final Clock clock;

    public SaleReportController(SaleReportService reportService, Clock clock) {
        this.reportService = reportService;
        this.clock = clock;
    }

    @GetMapping("/sales.{format}")
    public ResponseEntity<byte[]> download(
            @PathVariable String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        GeneratedDocument document = reportService.generate(
                ReportPeriod.resolve(from, to, clock), ReportFormat.parse(format));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(document.mediaType());
        headers.setContentDisposition(ContentDisposition.attachment().filename(document.filename()).build());
        headers.setCacheControl(CacheControl.noStore());
        return ResponseEntity.ok().headers(headers).body(document.content());
    }
}
