package ru.paul.wincustomf3.brand;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public final class BrandPayloadEncoder {

    public byte[] encode(final String brandText) {
        final byte[] textBytes = brandText.getBytes(StandardCharsets.UTF_8);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(textBytes.length + 5);

        writeVarInt(outputStream, textBytes.length);
        outputStream.writeBytes(textBytes);

        return outputStream.toByteArray();
    }

    private void writeVarInt(final ByteArrayOutputStream outputStream, int value) {
        while ((value & ~0x7F) != 0) {
            outputStream.write((value & 0x7F) | 0x80);
            value >>>= 7;
        }

        outputStream.write(value);
    }
}

