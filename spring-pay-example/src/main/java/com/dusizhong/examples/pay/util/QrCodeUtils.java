package com.dusizhong.examples.pay.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class QrCodeUtils {

    public static String generateQrCode(String payInfo) {
        String qrCode = null;
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(payInfo, BarcodeFormat.QR_CODE, 600, 600);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            Base64.Encoder encoder = Base64.getEncoder();
            qrCode = "data:image/png;base64," + encoder.encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return qrCode;
    }
}
