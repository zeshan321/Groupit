package com.groupit;

import com.parse.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class StringHandler {

    public enum Type {
        COMPRESS, DECOMPRESS
    }

    Type type;
    String data;

    public StringHandler(Type type, String data) {
        this.type = type;
        this.data = data;
    }

    public String run() {
        switch (type) {
            case COMPRESS:
                try {
                    return compress(data);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            case DECOMPRESS:
                try {
                    return decompressToString(data);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (DataFormatException e) {
                    e.printStackTrace();
                }
        }
        return null;
    }

    private String compress(String stringToCompress) throws UnsupportedEncodingException
    {
        byte[] compressedData = new byte[1024];
        byte[] stringAsBytes = stringToCompress.getBytes("UTF-8");

        Deflater compressor = new Deflater();
        compressor.setInput(stringAsBytes);
        compressor.finish();
        int compressedDataLength = compressor.deflate(compressedData);

        byte[] bytes = Arrays.copyOf(compressedData, compressedDataLength);
        return Base64.encodeBase64String(bytes);
    }

    private String decompressToString(String base64String) throws UnsupportedEncodingException, DataFormatException
    {
        byte[] compressedData = Base64.decodeBase64(base64String);

        Inflater deCompressor = new Inflater();
        deCompressor.setInput(compressedData, 0, compressedData.length);
        byte[] output = new byte[102400];
        int decompressedDataLength = deCompressor.inflate(output);
        deCompressor.end();

        return new String(output, 0, decompressedDataLength, "UTF-8");
    }
}