package com.eternitywall;

import com.eternitywall.http.Request;
import com.eternitywall.http.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
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
    private byte[] incomplete;
    private byte[] incompleteOts;
    private String incompleteOtsInfo;
    private byte[] helloworld;
    private byte[] helloworldOts;
    private byte[] merkle2Ots;
    private String merkle2OtsInfo;





    private String helloWorldHashHex="03ba204e50d126e4674c005e04d82e84c21366780af1f43bd54a37816b6ab340";
    
    
    private String baseUrl = "https://raw.githubusercontent.com/eternitywall/javascript-opentimestamps/master";

    @Before
    public void loadData() throws ExecutionException, InterruptedException, IOException {
        executor = Executors.newFixedThreadPool(4);
        Future<Response> incompleteFuture = executor.submit(new Request(new URL( baseUrl + "/examples/incomplete.txt")));
        Future<Response> incompleteOtsFuture = executor.submit(new Request(new URL( baseUrl + "/examples/incomplete.txt.ots")));
        Future<Response> incompleteOtsInfoFuture = executor.submit(new Request(new URL( baseUrl + "/examples/incomplete.txt.ots.info")));
        Future<Response> helloworldFuture = executor.submit(new Request(new URL( baseUrl + "/examples/hello-world.txt")));
        Future<Response> helloworldOtsFuture = executor.submit(new Request(new URL( baseUrl + "/examples/hello-world.txt.ots")));
        Future<Response> merkle2OtsFuture = executor.submit(new Request(new URL( baseUrl + "/examples/merkle2.txt.ots")));
        Future<Response> merkle2OtsInfoFuture = executor.submit(new Request(new URL( baseUrl + "/examples/merkle2.txt.ots.info")));

        incompleteOts = incompleteOtsFuture.get().getBytes();
        incomplete = incompleteFuture.get().getBytes();
        incompleteOtsInfo = incompleteOtsInfoFuture.get().getString();
        helloworld = helloworldFuture.get().getBytes();
        helloworldOts = helloworldOtsFuture.get().getBytes();
        merkle2Ots = merkle2OtsFuture.get().getBytes();
        merkle2OtsInfo = merkle2OtsInfoFuture.get().getString();
    }

    @Test
    public void info() throws ExecutionException, InterruptedException, IOException {
        String result = OpenTimestamps.info(incompleteOts);
        assertNotNull(result);
        assertNotNull(incompleteOtsInfo);
        boolean equals = result.equals(incompleteOtsInfo);
        assertEquals(incompleteOtsInfo, result);

        // Unknown operation tag: -1
        /*
        String result2 = OpenTimestamps.info(merkle2Ots);
        assertNotNull(result2);
        assertNotNull(merkle2OtsInfo);
        assertEquals(merkle2OtsInfo, result2);
        */
    }

    @Test
    public void stamp() throws NoSuchAlgorithmException, IOException, ExecutionException, InterruptedException {
        byte[] bytes = Utils.randBytes(32);
        byte[] ots = OpenTimestamps.stamp(new Hash(bytes));
        StreamDeserializationContext ctx = new StreamDeserializationContext(ots);
        DetachedTimestampFile detachedTimestampFile = DetachedTimestampFile.deserialize(ctx);
        byte[] digest = detachedTimestampFile.fileDigest();
        assertTrue(Arrays.equals(digest, bytes));

        byte[] ots2 = OpenTimestamps.stamp(helloworld);
        StreamDeserializationContext ctx2 = new StreamDeserializationContext(ots2);
        DetachedTimestampFile detachedTimestampFile2 = DetachedTimestampFile.deserialize(ctx2);
        byte[] digest2 = detachedTimestampFile2.fileDigest();
        assertTrue(Arrays.equals(digest2, Utils.hexToBytes(helloWorldHashHex)));

        byte[] ots3 = OpenTimestamps.stamp(new ByteArrayInputStream(helloworld));  //testing input stream
        StreamDeserializationContext ctx3 = new StreamDeserializationContext(ots3);
        DetachedTimestampFile detachedTimestampFile3 = DetachedTimestampFile.deserialize(ctx3);
        byte[] digest3 = detachedTimestampFile3.fileDigest();
        assertTrue(Arrays.equals(digest3, digest2));
    }

    @Test
    public void verify() throws NoSuchAlgorithmException, IOException, ExecutionException, InterruptedException {

        Long timestamp = OpenTimestamps.verify( helloworldOts , helloworld );
        assertEquals(1432827678L, timestamp.longValue());

        // verify on python call upgrade
        /*
        Long timestamp2 = OpenTimestamps.verify( incompleteOts, incomplete );
        assertEquals(1473227803L, timestamp2.longValue());
        */
    }

    @Test
    public void upgrade() throws ExecutionException, InterruptedException, IOException {
        /*
        byte[] upgraded = OpenTimestamps.upgrade(incompleteOts);
        Long timestamp = OpenTimestamps.verify( upgraded, incomplete );
        assertEquals(1473227803L, timestamp.longValue());
        */
    }


    @After
    public void tearDown() {
        executor.shutdown();
    }

}
