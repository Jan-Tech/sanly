package com.sanly.signature.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Service
public class QrCodeService {

    private final String verifyBaseUrl;

    public QrCodeService(@Value("${qr.verify-base-url:https://sanly.tm/verify}") String verifyBaseUrl) {
        this.verifyBaseUrl = verifyBaseUrl;
    }

    /**
     * Generates a 300x300 PNG QR code encoding the public verification URL for this signature.
     * The QR code can be embedded in signed documents so that anyone can verify authenticity
     * by scanning with a phone camera.
     *
     * @param signatureCode  the TM-SIG-YYYYNNNNNN code
     * @return PNG image bytes
     */
    public byte[] generateQrCode(String signatureCode) {
        try {
            String content = verifyBaseUrl + "/" + signatureCode;

            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN, 2
            );

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 300, 300, hints);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", stream);
            return stream.toByteArray();

        } catch (WriterException | IOException e) {
            throw new IllegalStateException("QR code generation failed for signatureCode=" + signatureCode, e);
        }
    }
}
