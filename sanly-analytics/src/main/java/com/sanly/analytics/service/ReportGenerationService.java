package com.sanly.analytics.service;

import com.opencsv.CSVWriter;
import com.sanly.analytics.client.NotificationClient;
import com.sanly.analytics.dto.request.GenerateReportRequest;
import com.sanly.analytics.dto.response.ReportStatusResponse;
import com.sanly.analytics.entity.*;
import com.sanly.analytics.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportGenerationService {

    private final ReportExportRepository exportRepo;
    private final ExportCodeService codeService;
    private final DailySnapshotRepository snapshotRepo;
    private final RegionalStatRepository regionalRepo;
    private final ServiceUsageStatRepository usageRepo;
    private final AnomalyTrendRepository anomalyRepo;
    private final AnalyticsOfficerRepository officerRepo;
    private final NotificationClient notificationClient;

    @Transactional
    public ReportStatusResponse initiateReport(GenerateReportRequest req, UUID officerId) {
        String code = codeService.generateCode();
        ReportExport export = ReportExport.builder()
                .exportId(UUID.randomUUID())
                .exportCode(code)
                .reportType(req.reportType().name())
                .generatedByOfficerId(officerId)
                .dateFrom(req.dateFrom())
                .dateTo(req.dateTo())
                .status("GENERATING")
                .generatedAt(LocalDateTime.now())
                .build();
        exportRepo.save(export);
        generateAsync(export.getExportId(), officerId);
        return toStatusResponse(export);
    }

    @Async("analyticsExecutor")
    public void generateAsync(UUID exportId, UUID officerId) {
        ReportExport export = exportRepo.findById(exportId).orElse(null);
        if (export == null) return;
        try {
            ReportType type = ReportType.valueOf(export.getReportType());
            List<DailySnapshot> snapshots = snapshotRepo.findBySnapshotDateBetweenOrderBySnapshotDateAsc(
                    export.getDateFrom() != null ? export.getDateFrom() : LocalDate.now().minusDays(30),
                    export.getDateTo() != null ? export.getDateTo() : LocalDate.now());

            byte[] pdfBytes = generatePdf(type, export, snapshots);
            byte[] csvBytes = generateCsv(snapshots);

            export.setReportData(pdfBytes);
            export.setReportDataCsv(csvBytes);
            export.setStatus("READY");
            export.setCompletedAt(LocalDateTime.now());
            export.setExpiresAt(LocalDateTime.now().plusHours(24));
            exportRepo.save(export);
            log.info("Report {} generated successfully ({} bytes PDF)", export.getExportCode(), pdfBytes.length);

            if (officerId != null) {
                officerRepo.findById(officerId).ifPresent(officer ->
                    notificationClient.send(null, "REPORT_GENERATED", "EN",
                            Map.of("reportType", export.getReportType(), "exportCode", export.getExportCode()))
                );
            }
        } catch (Exception e) {
            log.error("Report generation failed for {}: {}", exportId, e.getMessage(), e);
            export.setStatus("FAILED");
            export.setFailureReason(e.getMessage());
            exportRepo.save(export);
        }
    }

    private byte[] generatePdf(ReportType type, ReportExport export, List<DailySnapshot> snapshots) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font mono = new PDType1Font(Standard14Fonts.FontName.COURIER);

            float margin = 50;
            float pageWidth = page.getMediaBox().getWidth();
            float y = page.getMediaBox().getHeight() - margin;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                // Header
                cs.beginText();
                cs.setFont(bold, 14);
                cs.newLineAtOffset(margin, y);
                cs.showText("SANLY PLATFORM — GOVERNMENT OF TURKMENISTAN");
                cs.endText();
                y -= 20;

                cs.beginText();
                cs.setFont(bold, 11);
                cs.newLineAtOffset(margin, y);
                cs.showText(type.name().replace("_", " ") + " REPORT");
                cs.endText();
                y -= 16;

                cs.beginText();
                cs.setFont(regular, 9);
                cs.newLineAtOffset(margin, y);
                String dateRange = "Period: " + (export.getDateFrom() != null ? export.getDateFrom() : "N/A")
                        + " to " + (export.getDateTo() != null ? export.getDateTo() : "N/A");
                cs.showText(dateRange);
                cs.endText();
                y -= 13;

                cs.beginText();
                cs.setFont(regular, 9);
                cs.newLineAtOffset(margin, y);
                cs.showText("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                cs.endText();
                y -= 20;

                // Horizontal rule
                cs.moveTo(margin, y);
                cs.lineTo(pageWidth - margin, y);
                cs.stroke();
                y -= 15;

                // Summary section
                DailySnapshot latest = snapshots.isEmpty() ? DailySnapshot.builder().build() : snapshots.get(snapshots.size() - 1);
                y = writeSection(cs, bold, "KEY METRICS", margin, y);
                y = writeKv(cs, regular, mono, margin, y, "Total Citizens", String.valueOf(latest.getTotalCitizens()));
                y = writeKv(cs, regular, mono, margin, y, "Active Citizens", String.valueOf(latest.getActiveCitizens()));
                y = writeKv(cs, regular, mono, margin, y, "Active Businesses", String.valueOf(latest.getActiveBusinesses()));
                y = writeKv(cs, regular, mono, margin, y, "Open Court Cases", String.valueOf(latest.getOpenCases()));
                y = writeKv(cs, regular, mono, margin, y, "Open Anomaly Alerts", String.valueOf(latest.getOpenAnomalyAlerts()));
                y = writeKv(cs, regular, mono, margin, y, "Bridge Exchanges Today", String.valueOf(latest.getExchangesToday()));
                y -= 10;

                // Trend table
                if (!snapshots.isEmpty()) {
                    y = writeSection(cs, bold, "DAILY TREND (" + snapshots.size() + " days)", margin, y);
                    y = writeTableHeader(cs, bold, mono, margin, y, pageWidth,
                            "Date", "New Citizens", "New Biz", "Exchanges", "Anomalies");
                    for (DailySnapshot s : snapshots) {
                        if (y < 80) break;
                        y = writeTableRow(cs, regular, mono, margin, y, pageWidth,
                                s.getSnapshotDate().toString(),
                                String.valueOf(s.getNewRegistrationsToday()),
                                String.valueOf(s.getNewBusinessesToday()),
                                String.valueOf(s.getExchangesToday()),
                                String.valueOf(s.getOpenAnomalyAlerts()));
                    }
                }

                // Regional section
                List<RegionalStat> regions = regionalRepo.findLatestAll();
                if (!regions.isEmpty()) {
                    y -= 10;
                    y = writeSection(cs, bold, "REGIONAL BREAKDOWN", margin, y);
                    y = writeTableHeader(cs, bold, mono, margin, y, pageWidth,
                            "Region", "Citizens", "Businesses", "Properties", "Appointments");
                    for (RegionalStat r : regions) {
                        if (y < 80) break;
                        y = writeTableRow(cs, regular, mono, margin, y, pageWidth,
                                r.getRegion(),
                                String.valueOf(r.getCitizenCount()),
                                String.valueOf(r.getBusinessCount()),
                                String.valueOf(r.getPropertyCount()),
                                String.valueOf(r.getAppointmentCount()));
                    }
                }

                // Footer
                cs.beginText();
                cs.setFont(regular, 7);
                cs.newLineAtOffset(margin, 30);
                cs.showText("CONFIDENTIAL — Government of Turkmenistan — SANLY Platform — Generated: "
                        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                cs.endText();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    private float writeSection(PDPageContentStream cs, PDType1Font bold, String title, float x, float y) throws IOException {
        cs.beginText();
        cs.setFont(bold, 10);
        cs.newLineAtOffset(x, y);
        cs.showText(title);
        cs.endText();
        return y - 14;
    }

    private float writeKv(PDPageContentStream cs, PDType1Font label, PDType1Font value,
                           float x, float y, String k, String v) throws IOException {
        cs.beginText();
        cs.setFont(label, 9);
        cs.newLineAtOffset(x, y);
        cs.showText(k + ": ");
        cs.endText();
        cs.beginText();
        cs.setFont(value, 9);
        cs.newLineAtOffset(x + 160, y);
        cs.showText(v);
        cs.endText();
        return y - 12;
    }

    private float writeTableHeader(PDPageContentStream cs, PDType1Font bold, PDType1Font mono,
                                    float x, float y, float pageWidth, String... cols) throws IOException {
        float colW = (pageWidth - 2 * x) / cols.length;
        cs.beginText();
        cs.setFont(bold, 8);
        cs.newLineAtOffset(x, y);
        for (String col : cols) {
            cs.showText(pad(col, 18));
        }
        cs.endText();
        y -= 2;
        cs.moveTo(x, y);
        cs.lineTo(pageWidth - x, y);
        cs.stroke();
        return y - 11;
    }

    private float writeTableRow(PDPageContentStream cs, PDType1Font regular, PDType1Font mono,
                                 float x, float y, float pageWidth, String... cols) throws IOException {
        cs.beginText();
        cs.setFont(regular, 8);
        cs.newLineAtOffset(x, y);
        for (String col : cols) {
            cs.showText(pad(col, 18));
        }
        cs.endText();
        return y - 11;
    }

    private String pad(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s.substring(0, width - 1) + " ";
        return s + " ".repeat(width - s.length());
    }

    private byte[] generateCsv(List<DailySnapshot> snapshots) {
        StringWriter sw = new StringWriter();
        try (CSVWriter csv = new CSVWriter(sw)) {
            csv.writeNext(new String[]{"date", "total_citizens", "active_citizens", "new_registrations",
                    "total_businesses", "active_businesses", "new_businesses",
                    "licenses_issued", "exchanges_today", "appointments_today",
                    "no_show_count", "avg_rating", "open_anomaly_alerts"});
            for (DailySnapshot s : snapshots) {
                csv.writeNext(new String[]{
                        s.getSnapshotDate().toString(),
                        String.valueOf(s.getTotalCitizens()),
                        String.valueOf(s.getActiveCitizens()),
                        String.valueOf(s.getNewRegistrationsToday()),
                        String.valueOf(s.getTotalBusinesses()),
                        String.valueOf(s.getActiveBusinesses()),
                        String.valueOf(s.getNewBusinessesToday()),
                        String.valueOf(s.getLicensesIssuedToday()),
                        String.valueOf(s.getExchangesToday()),
                        String.valueOf(s.getAppointmentsToday()),
                        String.valueOf(s.getNoShowCount()),
                        String.valueOf(s.getAvgAppointmentRating()),
                        String.valueOf(s.getOpenAnomalyAlerts())
                });
            }
        } catch (IOException e) {
            log.warn("CSV generation error: {}", e.getMessage());
        }
        return sw.toString().getBytes(StandardCharsets.UTF_8);
    }

    public ReportStatusResponse getStatus(String exportCode) {
        return exportRepo.findByExportCode(exportCode)
                .map(this::toStatusResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found: " + exportCode));
    }

    public byte[] downloadPdf(String exportCode) {
        ReportExport export = exportRepo.findByExportCode(exportCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
        if (!"READY".equals(export.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Report not ready: " + export.getStatus());
        }
        if (export.getExpiresAt() != null && export.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Report has expired");
        }
        return export.getReportData();
    }

    public List<ReportStatusResponse> listReports(UUID officerId) {
        return exportRepo.findAllByOrderByGeneratedAtDesc(
                        org.springframework.data.domain.PageRequest.of(0, 50)).stream()
                .map(this::toStatusResponse)
                .collect(Collectors.toList());
    }

    private ReportStatusResponse toStatusResponse(ReportExport e) {
        return new ReportStatusResponse(
                e.getExportCode(), e.getStatus(), e.getReportType(),
                e.getDateFrom(), e.getDateTo(),
                e.getGeneratedAt(), e.getCompletedAt(), e.getExpiresAt(),
                e.getFailureReason()
        );
    }
}
