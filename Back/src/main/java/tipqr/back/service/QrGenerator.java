package tipqr.back.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * Genera la imagen PNG de un código QR a partir de su contenido (la URL pública).
 */
@Component
public class QrGenerator {

    private static final int TAMANIO_DEFAULT = 320;

    public byte[] generarPng(String contenido) {
        return generarPng(contenido, TAMANIO_DEFAULT);
    }

    public byte[] generarPng(String contenido, int tamanio) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN, 1,
                    EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix matrix = writer.encode(contenido, BarcodeFormat.QR_CODE, tamanio, tamanio, hints);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar la imagen del QR", e);
        }
    }
}
