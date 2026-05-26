package com.sanly.documents.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;

@Service
public class QrCodeService {

    @Value("${qr.verify-base-url:https://sanly.tm/verify-cert}")
    private String verifyBaseUrl;

    private static final int QR_SIZE = 200;

    /**
     * Generates a 200x200 PNG QR code encoding the verification URL for a certificate.
     *
     * @param certificateCode the certificate code to embed in the QR
     * @return PNG bytes of the QR code
     */
    public byte[] generateQrCode(String certificateCode) {
        String url = verifyBaseUrl + "/" + certificateCode;
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN, 1
            );
            BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(MatrixToImageWriter.toBufferedImage(matrix), "PNG", baos);
            return baos.toByteArray();
        } catch (WriterException | IOException e) {
            throw new IllegalStateException("Failed to generate QR code for certificate: " + certificateCode, e);
        }
    }

    /**
     * Generates a BufferedImage of the QR code (used for PDF embedding).
     */
    public BufferedImage generateQrBufferedImage(String certificateCode) {
        String url = verifyBaseUrl + "/" + certificateCode;
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN, 1
            );
            BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints);
            return MatrixToImageWriter.toBufferedImage(matrix);
        } catch (WriterException e) {
            throw new IllegalStateException("Failed to generate QR BufferedImage for: " + certificateCode, e);
        }
    }
}
