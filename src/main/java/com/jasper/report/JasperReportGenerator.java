package com.jasper.report;

import com.jasper.entity.Sale;
import com.jasper.exception.ReportGenerationException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleDocxReportConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import net.sf.jasperreports.pdf.JRPdfExporter;
import net.sf.jasperreports.pdf.SimplePdfExporterConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JasperReportGenerator {

    private final JasperReport template;

    public JasperReportGenerator() {
        try (var input = new ClassPathResource("reports/sales-performance.jrxml").getInputStream()) {
            template = JasperCompileManager.compileReport(input);
        } catch (IOException | JRException exception) {
            throw new IllegalStateException("Unable to compile the Jasper report template", exception);
        }
    }

    public GeneratedDocument generate(List<Sale> sales, Map<String, Object> reportParameters,
                                      ReportFormat format, String filename) {
        try {
            Map<String, Object> parameters = new HashMap<>(reportParameters);
            parameters.put(JRParameter.IS_IGNORE_PAGINATION, format == ReportFormat.XLSX);
            JasperPrint print = JasperFillManager.fillReport(
                    template, parameters, new JRBeanCollectionDataSource(sales));
            return new GeneratedDocument(export(print, format), filename, format.mediaType());
        } catch (JRException exception) {
            throw new ReportGenerationException("Unable to generate the sales report", exception);
        }
    }

    private byte[] export(JasperPrint print, ReportFormat format) throws JRException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        switch (format) {
            case PDF -> exportPdf(print, output);
            case DOCX -> exportDocx(print, output);
            case XLSX -> exportXlsx(print, output);
        }
        return output.toByteArray();
    }

    private void exportPdf(JasperPrint print, ByteArrayOutputStream output) throws JRException {
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(print));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));
        SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
        configuration.setMetadataTitle("گزارش فروش");
        exporter.setConfiguration(configuration);
        exporter.exportReport();
    }

    private void exportDocx(JasperPrint print, ByteArrayOutputStream output) throws JRException {
        JRDocxExporter exporter = new JRDocxExporter();
        exporter.setExporterInput(new SimpleExporterInput(print));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));
        SimpleDocxReportConfiguration configuration = new SimpleDocxReportConfiguration();
        configuration.setFlexibleRowHeight(true);
        exporter.setConfiguration(configuration);
        exporter.exportReport();
    }

    private void exportXlsx(JasperPrint print, ByteArrayOutputStream output) throws JRException {
        JRXlsxExporter exporter = new JRXlsxExporter();
        exporter.setExporterInput(new SimpleExporterInput(print));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));
        SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
        configuration.setSheetNames(new String[]{"فروش"});
        configuration.setDetectCellType(true);
        configuration.setRemoveEmptySpaceBetweenRows(true);
        configuration.setRemoveEmptySpaceBetweenColumns(true);
        exporter.setConfiguration(configuration);
        exporter.exportReport();
    }
}
