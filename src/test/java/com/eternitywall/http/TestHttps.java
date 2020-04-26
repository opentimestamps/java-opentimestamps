package com.eternitywall.http;

import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertNotNull;

public class TestHttps {

    @Test
    public void open() throws Exception {
        //Here casatta machine fails only on ots.eternitywall.it
        Request request = new Request(new URL("https://finney.calendar.eternitywall.com"));
        String string = request.call().getString();
        assertNotNull(string);
    }
}
