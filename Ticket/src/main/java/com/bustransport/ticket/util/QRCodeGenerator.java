package com.bustransport.ticket.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class QRCodeGenerator {

    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;

    /**
     * Generate a unique QR code string
     */
    public String generateUniqueQRCode(Long ticketId, Long userId) {
        return String.format("TICKET-%d-%d-%s", ticketId, userId, UUID.randomUUID().toString());
    }

    /**
     * Generate QR code image as base64 string
     */
    public String generateQRCodeImage(String qrCodeText) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = qrCodeWriter.encode(
                qrCodeText,
                BarcodeFormat.QR_CODE,
                QR_CODE_WIDTH,
                QR_CODE_HEIGHT,
                hints
            );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] qrCodeBytes = outputStream.toByteArray();

            return Base64.getEncoder().encodeToString(qrCodeBytes);
        } catch (WriterException | IOException e) {
            log.error("Error generating QR code image", e);
            throw new RuntimeException("Failed to generate QR code image", e);
        }
    }

    /**
     * Generate QR code image as byte array
     */
    public byte[] generateQRCodeImageBytes(String qrCodeText) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = qrCodeWriter.encode(
                qrCodeText,
                BarcodeFormat.QR_CODE,
                QR_CODE_WIDTH,
                QR_CODE_HEIGHT,
                hints
            );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (WriterException | IOException e) {
            log.error("Error generating QR code image bytes", e);
            throw new RuntimeException("Failed to generate QR code image", e);
        }
    }
}
