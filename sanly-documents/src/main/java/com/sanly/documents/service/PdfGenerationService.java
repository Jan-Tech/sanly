package com.sanly.documents.service;

import com.sanly.documents.entity.DocumentCertificate;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
public class PdfGenerationService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm");
    private static final float PAGE_WIDTH  = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float MARGIN      = 50f;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;

    private final QrCodeService qrCodeService;

    public PdfGenerationService(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    /**
     * Generates a certified PDF document for the given certificate and field data.
     *
     * @param cert      the stored certificate entity
     * @param fieldData key-value pairs of document fields for display
     * @return PDF bytes
     */
    public byte[] generateCertificatePdf(DocumentCertificate cert, Map<String, String> fieldData) {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDFont fontBold    = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDFont fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont fontCourier = new PDType1Font(Standard14Fonts.FontName.COURIER_BOLD);

            // --- First pass: main content ---
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                // ---- Background: light gray border rectangle ----
                cs.setNonStrokingColor(0.95f, 0.95f, 0.95f);
                cs.addRect(MARGIN - 10, MARGIN - 10, CONTENT_WIDTH + 20, PAGE_HEIGHT - 2 * MARGIN + 20);
                cs.fill();

                // Reset to black
                cs.setNonStrokingColor(0f, 0f, 0f);

                float y = PAGE_HEIGHT - MARGIN - 10;

                // ---- Title area ----
                cs.setFont(fontBold, 14);
                cs.setNonStrokingColor(0.09f, 0.28f, 0.52f); // dark blue
                String title1 = "SANLY -- Digital Government of Turkmenistan";
                float title1Width = fontBold.getStringWidth(title1) / 1000f * 14f;
                cs.beginText();
                cs.newLineAtOffset((PAGE_WIDTH - title1Width) / 2f, y);
                cs.showText(title1);
                cs.endText();
                y -= 22;

                // ---- Subtitle ----
                cs.setFont(fontBold, 11);
                cs.setNonStrokingColor(0.2f, 0.2f, 0.2f);
                String subtitle = "CERTIFIED DIGITAL DOCUMENT";
                float subtitleWidth = fontBold.getStringWidth(subtitle) / 1000f * 11f;
                cs.beginText();
                cs.newLineAtOffset((PAGE_WIDTH - subtitleWidth) / 2f, y);
                cs.showText(subtitle);
                cs.endText();
                y -= 18;

                // ---- Document type label ----
                cs.setFont(fontBold, 13);
                cs.setNonStrokingColor(0.1f, 0.1f, 0.5f);
                String docTypeLabel = cert.getDocumentType().name().replace("_", " ");
                float dtLabelWidth = fontBold.getStringWidth(docTypeLabel) / 1000f * 13f;
                cs.beginText();
                cs.newLineAtOffset((PAGE_WIDTH - dtLabelWidth) / 2f, y);
                cs.showText(docTypeLabel);
                cs.endText();
                y -= 16;

                // ---- Certificate title ----
                if (cert.getTitle() != null && !cert.getTitle().isBlank()) {
                    cs.setFont(fontRegular, 10);
                    cs.setNonStrokingColor(0.3f, 0.3f, 0.3f);
                    String certTitle = cert.getTitle();
                    float ctWidth = fontRegular.getStringWidth(certTitle) / 1000f * 10f;
                    cs.beginText();
                    cs.newLineAtOffset((PAGE_WIDTH - ctWidth) / 2f, y);
                    cs.showText(certTitle);
                    cs.endText();
                    y -= 14;
                }

                // ---- Horizontal separator ----
                y -= 6;
                cs.setStrokingColor(0.09f, 0.28f, 0.52f);
                cs.setLineWidth(1.5f);
                cs.moveTo(MARGIN, y);
                cs.lineTo(PAGE_WIDTH - MARGIN, y);
                cs.stroke();
                y -= 14;

                // ---- Holder info block ----
                cs.setFont(fontBold, 10);
                cs.setNonStrokingColor(0f, 0f, 0f);
                cs.beginText();
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("Document Holder:");
                cs.endText();

                cs.setFont(fontRegular, 10);
                cs.beginText();
                cs.newLineAtOffset(MARGIN + 120, y);
                cs.showText(cert.getHolderName());
                cs.endText();
                y -= 14;

                cs.setFont(fontBold, 10);
                cs.beginText();
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("National ID:");
                cs.endText();

                cs.setFont(fontRegular, 10);
                cs.beginText();
                cs.newLineAtOffset(MARGIN + 120, y);
                cs.showText(maskNationalId(cert.getCitizenNationalId()));
                cs.endText();
                y -= 14;

                // ---- Field table from source data ----
                if (fieldData != null && !fieldData.isEmpty()) {
                    y -= 4;
                    cs.setStrokingColor(0.8f, 0.8f, 0.8f);
                    cs.setLineWidth(0.5f);
                    cs.moveTo(MARGIN, y);
                    cs.lineTo(PAGE_WIDTH - MARGIN, y);
                    cs.stroke();
                    y -= 12;

                    cs.setFont(fontBold, 10);
                    cs.setNonStrokingColor(0.09f, 0.28f, 0.52f);
                    cs.beginText();
                    cs.newLineAtOffset(MARGIN, y);
                    cs.showText("Document Details");
                    cs.endText();
                    y -= 14;

                    cs.setNonStrokingColor(0f, 0f, 0f);
                    for (Map.Entry<String, String> entry : fieldData.entrySet()) {
                        if (y < MARGIN + 80) break; // avoid overflow
                        String key   = entry.getKey();
                        String value = entry.getValue();

                        cs.setFont(fontBold, 9);
                        cs.beginText();
                        cs.newLineAtOffset(MARGIN, y);
                        cs.showText(formatKey(key) + ":");
                        cs.endText();

                        cs.setFont(fontRegular, 9);
                        String displayValue = value != null ? truncate(value, 60) : "-";
                        cs.beginText();
                        cs.newLineAtOffset(MARGIN + 140, y);
                        cs.showText(displayValue);
                        cs.endText();
                        y -= 13;
                    }
                }

                y -= 6;

                // ---- Separator ----
                cs.setStrokingColor(0.09f, 0.28f, 0.52f);
                cs.setLineWidth(1f);
                cs.moveTo(MARGIN, y);
                cs.lineTo(PAGE_WIDTH - MARGIN, y);
                cs.stroke();
                y -= 14;

                // ---- Certificate code box ----
                float boxX = MARGIN;
                float boxY = y - 22;
                float boxW = CONTENT_WIDTH * 0.65f;
                float boxH = 22f;

                cs.setNonStrokingColor(0.93f, 0.96f, 1f);
                cs.addRect(boxX, boxY, boxW, boxH);
                cs.fill();

                cs.setStrokingColor(0.09f, 0.28f, 0.52f);
                cs.setLineWidth(0.8f);
                cs.addRect(boxX, boxY, boxW, boxH);
                cs.stroke();

                cs.setNonStrokingColor(0f, 0f, 0f);
                cs.setFont(fontBold, 10);
                cs.beginText();
                cs.newLineAtOffset(boxX + 6, boxY + 7);
                cs.showText("Certificate Code: ");
                cs.endText();

                cs.setFont(fontCourier, 10);
                cs.beginText();
                cs.newLineAtOffset(boxX + 115, boxY + 7);
                cs.showText(cert.getCertificateCode());
                cs.endText();

                y = boxY - 14;

                // ---- Issue date / expiry ----
                cs.setFont(fontBold, 9);
                cs.setNonStrokingColor(0.2f, 0.2f, 0.2f);
                cs.beginText();
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("Issued At: ");
                cs.endText();

                cs.setFont(fontRegular, 9);
                cs.beginText();
                cs.newLineAtOffset(MARGIN + 65, y);
                cs.showText(cert.getIssuedAt().format(DT_FMT));
                cs.endText();
                y -= 13;

                if (cert.getExpiresAt() != null) {
                    cs.setFont(fontBold, 9);
                    cs.beginText();
                    cs.newLineAtOffset(MARGIN, y);
                    cs.showText("Valid Until: ");
                    cs.endText();

                    cs.setFont(fontRegular, 9);
                    cs.beginText();
                    cs.newLineAtOffset(MARGIN + 65, y);
                    cs.showText(cert.getExpiresAt().format(DT_FMT));
                    cs.endText();
                    y -= 13;
                } else {
                    cs.setFont(fontBold, 9);
                    cs.beginText();
                    cs.newLineAtOffset(MARGIN, y);
                    cs.showText("Valid Until: ");
                    cs.endText();
                    cs.setFont(fontRegular, 9);
                    cs.beginText();
                    cs.newLineAtOffset(MARGIN + 65, y);
                    cs.showText("No expiry");
                    cs.endText();
                    y -= 13;
                }

                // ---- Source service ----
                cs.setFont(fontBold, 9);
                cs.beginText();
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("Source Service: ");
                cs.endText();
                cs.setFont(fontRegular, 9);
                cs.beginText();
                cs.newLineAtOffset(MARGIN + 80, y);
                cs.showText(cert.getSourceService().name());
                cs.endText();
                y -= 20;

                // ---- QR code ----
                try {
                    BufferedImage qrImage = qrCodeService.generateQrBufferedImage(cert.getCertificateCode());
                    PDImageXObject qrPdImage = LosslessFactory.createFromImage(doc, qrImage);
                    float qrSize = 80f;
                    float qrX = PAGE_WIDTH - MARGIN - qrSize;
                    // Place QR at right side, vertically ~1/3 from bottom
                    float qrY = MARGIN + 90;
                    cs.drawImage(qrPdImage, qrX, qrY, qrSize, qrSize);

                    cs.setFont(fontRegular, 7);
                    cs.setNonStrokingColor(0.4f, 0.4f, 0.4f);
                    cs.beginText();
                    cs.newLineAtOffset(qrX, qrY - 10);
                    cs.showText("Scan to verify");
                    cs.endText();
                } catch (Exception qrEx) {
                    log.warn("QR code embedding failed for cert {}: {}", cert.getCertificateCode(), qrEx.getMessage());
                }

                // ---- Footer ----
                float footerY = MARGIN + 20;
                cs.setStrokingColor(0.8f, 0.8f, 0.8f);
                cs.setLineWidth(0.5f);
                cs.moveTo(MARGIN, footerY + 14);
                cs.lineTo(PAGE_WIDTH - MARGIN, footerY + 14);
                cs.stroke();

                cs.setFont(fontRegular, 7);
                cs.setNonStrokingColor(0.4f, 0.4f, 0.4f);
                String footer1 = "This is an officially certified digital document issued by SANLY. " +
                                 "Verify at sanly.tm/verify-cert/" + cert.getCertificateCode();
                cs.beginText();
                cs.newLineAtOffset(MARGIN, footerY + 5);
                cs.showText(footer1);
                cs.endText();

                cs.setFont(fontRegular, 7);
                cs.beginText();
                cs.newLineAtOffset(MARGIN, footerY - 5);
                cs.showText("Hash: " + cert.getVerificationHash());
                cs.endText();
            }

            // --- Second pass: watermark overlay ---
            try (PDPageContentStream watermarkCs = new PDPageContentStream(
                    doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

                watermarkCs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 42);
                watermarkCs.setNonStrokingColor(0.87f, 0.87f, 0.87f); // very light gray
                watermarkCs.saveGraphicsState();

                float centerX = PAGE_WIDTH / 2f;
                float centerY = PAGE_HEIGHT / 2f;

                // Rotate 45 degrees around center of page
                watermarkCs.transform(Matrix.getRotateInstance(Math.toRadians(45), centerX, centerY));

                watermarkCs.beginText();
                // Offset so the text is centered after rotation
                String watermarkText = "CERTIFIED DIGITAL COPY";
                float textWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
                        .getStringWidth(watermarkText) / 1000f * 42f;
                watermarkCs.newLineAtOffset(-textWidth / 2f, 0);
                watermarkCs.showText(watermarkText);
                watermarkCs.endText();

                watermarkCs.restoreGraphicsState();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate PDF for certificate " + cert.getCertificateCode(), e);
        }
    }

    private String maskNationalId(String nationalId) {
        if (nationalId == null || nationalId.length() < 4) return "***";
        return "***" + nationalId.substring(nationalId.length() - 4) + "***";
    }

    private String formatKey(String key) {
        if (key == null) return "";
        return key.substring(0, 1).toUpperCase()
                + key.substring(1).replace("_", " ").replace("-", " ");
    }

    private String truncate(String value, int maxLen) {
        if (value == null) return "";
        return value.length() > maxLen ? value.substring(0, maxLen - 3) + "..." : value;
    }
}
