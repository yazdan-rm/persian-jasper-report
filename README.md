# Persian Jasper Report

Small Spring Boot + JasperReports sample focused on one entity and one report.

## Structure

- `entity/Sale` — the only JPA entity.
- `repository/SaleRepository` — date-range data access.
- `service/SaleReportService` — report parameters and simple chart data.
- `report/JasperReportGenerator` — JRXML compilation and PDF/DOCX/XLSX export.
- `controller/SaleReportController` — report download endpoint.
- `src/main/resources/reports/sales-performance.jrxml` — preserved Persian dashboard design with native Jasper bar and pie charts.

## Requirements

- Java 21
- Maven 3.9+
- Oracle 19c or newer

The default database is Oracle Free at `localhost:1521/FREE` with username/password `jmix`.

## Run

```powershell
mvn spring-boot:run
```

The optional demo seed inserts Persian sales rows when the `sale` table is empty.

## Download

```text
GET /api/reports/sales.{format}?from=yyyy-MM-dd&to=yyyy-MM-dd
```

Supported formats are `pdf`, `docx`, and `xlsx`.

## Configuration

| Environment variable | Default | Purpose |
| --- | --- | --- |
| `DATABASE_USERNAME` | `jmix` | Oracle username |
| `DATABASE_PASSWORD` | `jmix` | Oracle password |
| `DEMO_DATA_ENABLED` | `true` | Inserts Persian demo rows when empty |
| `REPORT_TIME_ZONE` | `Asia/Tehran` | Report timestamp zone |
| `REPORT_CURRENCY` | `ریال` | Currency displayed in the report |

## Test

```powershell
mvn test
```

The focused test compiles the JRXML, renders the native Jasper charts, and generates all three formats under `target/generated-reports`.
