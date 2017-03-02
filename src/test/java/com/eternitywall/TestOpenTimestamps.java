package com.eternitywall;

import com.eternitywall.http.Request;
import com.eternitywall.http.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
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
    private String helloWorldHashHex="03ba204e50d126e4674c005e04d82e84c21366780af1f43bd54a37816b6ab340";
    
    
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

    @Test
    public void stamp() throws NoSuchAlgorithmException, IOException, ExecutionException, InterruptedException {
        /*byte[] bytes = Utils.randBytes(32);
        byte[] ots = OpenTimestamps.stamp(bytes);
        StreamDeserializationContext ctx = new StreamDeserializationContext(ots);
        DetachedTimestampFile detachedTimestampFile = DetachedTimestampFile.deserialize(ctx);
        assertEquals(detachedTimestampFile.fileDigest(), bytes);
*/
        byte[] ots2 = OpenTimestamps.stamp(helloworldFuture.get().getStream());
        StreamDeserializationContext ctx2 = new StreamDeserializationContext(ots2);
        DetachedTimestampFile detachedTimestampFile2 = DetachedTimestampFile.deserialize(ctx2);
        assertEquals(detachedTimestampFile2.fileDigest(), Utils.hexToBytes(helloWorldHashHex));
    }

    @Test
    public void verify() throws NoSuchAlgorithmException, IOException, ExecutionException, InterruptedException {

        Long timestamp = OpenTimestamps.verify( helloworldOtsFuture.get().getBytes(), helloworldFuture.get().getStream() );
        assertEquals(1438269988L, timestamp.longValue());

    }


    @After
    public void tearDown() {
        executor.shutdown();
    }

}
