package com.eternitywall.http;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Response {
    private InputStream stream;

    public Response(InputStream stream) {
        this.stream = stream;
    }

    public InputStream getStream() {
        return this.stream;
    }

    public String getString() throws IOException {
        return new String(getBytes(), StandardCharsets.UTF_8);
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

    public JSONObject getJson() throws IOException, JSONException {
        String jsonString = getString();
        JSONObject json = new JSONObject(jsonString);
        return json;
    }
}