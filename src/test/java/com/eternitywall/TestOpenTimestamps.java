package com.eternitywall;

import com.eternitywall.http.Request;
import com.eternitywall.http.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by casatta on 28/02/17.
 */
public class TestOpenTimestamps {
    private ExecutorService executor;
    private Future<Response> incompleteOtsInfoFuture;
    private Future<Response> incompleteOtsFuture;
    private Future<Response> incompleteFuture;
    private Future<Response> helloworldOtsFuture;
    private Future<Response> helloworldFuture;
    private Future<Response> merkle2OtsFuture;
    private Future<Response> merkle2OtsInfoFuture;
    
    
    private String baseUrl = "https://raw.githubusercontent.com/eternitywall/javascript-opentimestamps/master";

    @Before
    public void loadData() throws ExecutionException, InterruptedException, MalformedURLException {
        executor = Executors.newFixedThreadPool(4);
        incompleteOtsInfoFuture = executor.submit(new Request(new URL( baseUrl + "/examples/incomplete.txt.ots.info")));
        incompleteOtsFuture = executor.submit(new Request(new URL( baseUrl + "/examples/incomplete.txt.ots")));
        incompleteFuture = executor.submit(new Request(new URL( baseUrl + "/examples/incomplete.txt")));
        helloworldOtsFuture = executor.submit(new Request(new URL( baseUrl + "/examples/hello-world.txt.ots")));
        helloworldFuture = executor.submit(new Request(new URL( baseUrl + "/examples/hello-world.txt")));
        merkle2OtsFuture = executor.submit(new Request(new URL( baseUrl + "/examples/merkle2.txt.ots")));
        merkle2OtsInfoFuture = executor.submit(new Request(new URL( baseUrl + "/examples/merkle2.txt.ots.info")));
    }

    @Test
    public void info() throws ExecutionException, InterruptedException, IOException {
        byte[] incompleteOts = incompleteOtsFuture.get().getBytes();
        String result = OpenTimestamps.info(incompleteOts);
        String expectedResult = incompleteOtsInfoFuture.get().getString();
        assertNotNull(result);
        assertNotNull(expectedResult);
        boolean equals = result.equals(expectedResult);
        assertEquals(expectedResult, result);
    }

    @After
    public void tearDown() {
        executor.shutdown();
    }

}
