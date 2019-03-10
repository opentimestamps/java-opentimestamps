package com.eternitywall.http;

import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertEquals;

public class TestHttp {

    @Test
    public void test() throws Exception {
        Request request = new Request(new URL("http://httpbin.org/status/418"));
        Response call = request.call();
        assertEquals((Integer) 418, call.getStatus());
    }
}
