package com.eternitywall;

import com.eternitywall.http.Request;
import org.junit.Test;

import java.net.URL;

/**
 * Created by casatta on 28/02/17.
 */
public class TestHttps {

    @Test
    public void open() throws Exception {
        Request request = new Request(new URL("https://ots.eternitywall.it"));
        request.call().getString();
    }
}
