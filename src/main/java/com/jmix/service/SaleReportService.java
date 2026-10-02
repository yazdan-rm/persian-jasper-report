package com.jmix.service;

import com.jmix.entity.Sale;
import com.jmix.exception.ReportGenerationException;
import com.jmix.report.GeneratedDocument;
import com.jmix.report.JasperReportGenerator;
import com.jmix.report.ReportFormat;
import com.jmix.report.ReportPeriod;
import com.jmix.repository.SaleRepository;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Clock;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@Service
public class SaleReportService {

    private static final Locale PERSIAN = Locale.forLanguageTag("fa-IR");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM uuuu", PERSIAN);
    private static final DateTimeFormatter GENERATED_FORMAT = DateTimeFormatter.ofPattern("dd MMMM uuuu، HH:mm", PERSIAN);

    private final SaleRepository repository;
    private final JasperReportGenerator reportGenerator;
    private final Clock clock;
    private final String currency;

    public SaleReportService(SaleRepository repository, JasperReportGenerator reportGenerator, Clock clock,
                             @Value("${app.report.currency:ریال}") String currency) {
        this.repository = repository;
        this.reportGenerator = reportGenerator;
        this.clock = clock;
        this.currency = currency;
    }

    @Transactional(readOnly = true)
    public GeneratedDocument generate(ReportPeriod period, ReportFormat format) {
        List<Sale> sales = repository.findBySaleDateBetweenOrderBySaleDateAsc(period.from(), period.to());
        BigDecimal totalRevenue = sales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long unitsSold = sales.stream().mapToLong(Sale::getQuantity).sum();
        BigDecimal averageSale = sales.isEmpty()
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(sales.size()), 2, RoundingMode.HALF_UP);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("REPORT_LOGO", logo());
        parameters.put("COMPANY_NAME", "شرکت گزارش‌ساز پارسی");
        parameters.put("COMPANY_CONTACT", "تهران  •  report@example.com  •  ۰۲۱-۸۸۷۷۶۶۵۵");
        parameters.put("COMPANY_LEGAL", "سامانه نمونه پیاده‌سازی JasperReports");
        parameters.put("REPORT_PERIOD", DATE_FORMAT.format(period.from()) + " تا " + DATE_FORMAT.format(period.to()));
        parameters.put("GENERATED_AT", GENERATED_FORMAT.format(ZonedDateTime.now(clock)));
        parameters.put("CURRENCY", currency);
        parameters.put("ORDER_COUNT", Integer.toString(sales.size()));
        parameters.put("UNITS_SOLD", Long.toString(unitsSold));
        parameters.put("TOTAL_REVENUE", money(totalRevenue));
        parameters.put("AVERAGE_ORDER", money(averageSale));
        parameters.put("MONTHLY_DATA_SOURCE", new JRMapCollectionDataSource(monthlyRevenue(sales)));
        parameters.put("CATEGORY_DATA_SOURCE", new JRMapCollectionDataSource(categoryRevenue(sales)));

        String filename = "sales-report-%s-to-%s.%s"
                .formatted(period.from(), period.to(), format.extension());
        return reportGenerator.generate(sales, parameters, format, filename);
    }

    private String money(BigDecimal amount) {
        return new DecimalFormat("#,##0.00").format(amount) + " " + currency;
    }

    private List<Map<String, ?>> monthlyRevenue(List<Sale> sales) {
        Map<YearMonth, BigDecimal> totals = new TreeMap<>();
        sales.forEach(sale -> totals.merge(
                YearMonth.from(sale.getSaleDate()), sale.getTotalAmount(), BigDecimal::add));
        return totals.entrySet().stream()
                .<Map<String, ?>>map(entry -> chartPoint(
                        entry.getKey().getMonth().getDisplayName(java.time.format.TextStyle.SHORT, PERSIAN)
                                + " " + entry.getKey().getYear(),
                        entry.getValue()))
                .toList();
    }

    private List<Map<String, ?>> categoryRevenue(List<Sale> sales) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        sales.forEach(sale -> totals.merge(sale.getCategory(), sale.getTotalAmount(), BigDecimal::add));
        return totals.entrySet().stream()
                .<Map<String, ?>>map(entry -> chartPoint(entry.getKey(), entry.getValue()))
                .toList();
    }

    private Map<String, Object> chartPoint(String label, BigDecimal amount) {
        return Map.of("label", label, "amount", amount);
    }

    private byte[] logo() {
        try {
            return new ClassPathResource("report-assets/novatrade-logo.png").getContentAsByteArray();
        } catch (IOException exception) {
            throw new ReportGenerationException("Unable to load report logo", exception);
        }
    }
}
