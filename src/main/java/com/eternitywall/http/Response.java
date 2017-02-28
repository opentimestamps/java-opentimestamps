package com.eternitywall.http;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class Response {
    private InputStream stream;

    public Response(InputStream stream) {
        this.stream = stream;
    }

    public InputStream getStream() {
        return this.stream;
    }

    public String getString() throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(this.stream, StandardCharsets.UTF_8))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }

    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = this.stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }
}