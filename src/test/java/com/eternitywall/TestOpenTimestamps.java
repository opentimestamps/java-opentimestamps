package com.eternitywall;

import com.eternitywall.http.Request;
import com.eternitywall.http.Response;
import com.eternitywall.ots.*;
import com.eternitywall.ots.op.OpSHA256;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import static org.junit.Assert.*;

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
    private byte[] differentBlockchainOts;
    private String differentBlockchainOtsInfo;


    private String helloWorldHashHex="03ba204e50d126e4674c005e04d82e84c21366780af1f43bd54a37816b6ab340";
    
    
    private String baseUrl = "https://raw.githubusercontent.com/opentimestamps/java-opentimestamps/master";

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
        Future<Response> differentBlockchainOtsFuture = executor.submit(new Request(new URL( baseUrl + "/examples/different-blockchains.txt.ots")));
        Future<Response> differentBlockchainOtsInfoFuture = executor.submit(new Request(new URL( baseUrl + "/examples/different-blockchains.txt.ots.info")));

        incompleteOts = incompleteOtsFuture.get().getBytes();
        incomplete = incompleteFuture.get().getBytes();
        incompleteOtsInfo = incompleteOtsInfoFuture.get().getString();
        helloworld = helloworldFuture.get().getBytes();
        helloworldOts = helloworldOtsFuture.get().getBytes();
        merkle2Ots = merkle2OtsFuture.get().getBytes();
        merkle2OtsInfo = merkle2OtsInfoFuture.get().getString();
        differentBlockchainOts = differentBlockchainOtsFuture.get().getBytes();
        differentBlockchainOtsInfo = differentBlockchainOtsInfoFuture.get().getString();
    }

    @Test
    public void info() throws ExecutionException, InterruptedException, IOException {
        String result = OpenTimestamps.info(incompleteOts);
        assertNotNull(result);
        assertNotNull(incompleteOtsInfo);
        boolean equals = result.equals(incompleteOtsInfo);
        assertEquals(incompleteOtsInfo, result);

        String result2 = OpenTimestamps.info(merkle2Ots);
        assertNotNull(result2);
        assertNotNull(merkle2OtsInfo);
        assertEquals(merkle2OtsInfo, result2);

        String result3 = OpenTimestamps.info(differentBlockchainOts);
        assertNotNull(result3);
        assertNotNull(differentBlockchainOtsInfo);
        assertEquals(differentBlockchainOtsInfo, result3);
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
        assertTrue(Arrays.equals(digest2, DatatypeConverter.parseHexBinary(helloWorldHashHex)));

        byte[] ots3 = OpenTimestamps.stamp(new ByteArrayInputStream(helloworld));  //testing input stream
        StreamDeserializationContext ctx3 = new StreamDeserializationContext(ots3);
        DetachedTimestampFile detachedTimestampFile3 = DetachedTimestampFile.deserialize(ctx3);
        byte[] digest3 = detachedTimestampFile3.fileDigest();
        assertTrue(Arrays.equals(digest3, digest2));
    }

    @Test
    public void merkle() throws NoSuchAlgorithmException, IOException, ExecutionException, InterruptedException {

        List<byte[]> files = new ArrayList<>();
        files.add(helloworld);
        files.add(merkle2Ots);
        files.add(incomplete);

        List<DetachedTimestampFile> fileTimestamps = new ArrayList<>();
        for (byte[] file : files){
            InputStream is = new ByteArrayInputStream(helloworld);
            DetachedTimestampFile detachedTimestampFile = DetachedTimestampFile.from(new OpSHA256(), is);
            fileTimestamps.add( detachedTimestampFile );
        }

        Timestamp merkleTip = OpenTimestamps.makeMerkleTree(fileTimestamps);
        // For each fileTimestamps check the tip
        for (DetachedTimestampFile fileTimestamp : fileTimestamps){
            Set<byte[]> tips = fileTimestamp.getTimestamp().allTips();
            for (byte[] tip : tips){
                assertTrue( Arrays.equals(tip, merkleTip.getDigest()) );
            }
        }

    }

    @Test
    public void verify() throws NoSuchAlgorithmException, IOException, ExecutionException, InterruptedException {

        Long timestamp = OpenTimestamps.verify( helloworldOts , helloworld );
        assertEquals(1432827678L, timestamp.longValue());

        // verify on python call upgrade
        Long timestamp2 = OpenTimestamps.verify( incompleteOts, incomplete );
        assertEquals( null,timestamp2);

    }

    @Test
    public void upgrade() throws ExecutionException, InterruptedException, IOException, NoSuchAlgorithmException {

        byte[] upgraded = OpenTimestamps.upgrade(incompleteOts);
        Long timestamp = OpenTimestamps.verify( upgraded, incomplete );
        assertEquals(1473227803L, timestamp.longValue());

        byte[] hashBytes = Utils.randBytes(32);
        byte[] stamp = OpenTimestamps.stamp(new Hash(hashBytes));

        StreamDeserializationContext streamSerializationContext = new StreamDeserializationContext(stamp);
        Timestamp timestamp1 = Timestamp.deserialize(streamSerializationContext, hashBytes);
        boolean changed = OpenTimestamps.upgrade(timestamp1);
        assertFalse(changed);


        //byte[] upgrade = OpenTimestamps.upgrade(stamp);
        //assertTrue( Arrays.equals(stamp,upgrade) );   //FIXME this doesn't work, should it?

    }

    @Test
    public void test() throws ExecutionException, InterruptedException, IOException {

        byte []ots = DatatypeConverter.parseHexBinary("F0105C3F2B3F8524A32854E07AD8ADDE9C1908F10458D95A36F008088D287213A8B9880083DFE30D2EF90C8E2C2B68747470733A2F2F626F622E6274632E63616C656E6461722E6F70656E74696D657374616D70732E6F7267");
        byte []digest= DatatypeConverter.parseHexBinary("7aa9273d2a50dbe0cc5a6ccc444a5ca90c9491dd2ac91849e45195ae46f64fe352c3a63ba02775642c96131df39b5b85");
        Logger log = Logger.getLogger(MultiInsight.class.getName());
        //log.info("ots hex: " + DatatypeConverter.printHexBinary(ots));

        StreamDeserializationContext streamDeserializationContext = new StreamDeserializationContext(ots);
        Timestamp timestamp = Timestamp.deserialize(streamDeserializationContext, digest);
        //log.info(Timestamp.strTreeExtended(timestamp,2));

        StreamSerializationContext streamSerializationContext = new StreamSerializationContext();
        timestamp.serialize(streamSerializationContext);
        byte []otsBefore = streamSerializationContext.getOutput();
        //log.info("fullOts hex: " + DatatypeConverter.printHexBinary(otsBefore));

        //log.info("upgrading " + OpenTimestamps.info(timestamp));
        boolean changed = OpenTimestamps.upgrade(timestamp);
        assertTrue(changed);

        //streamSerializationContext = new StreamSerializationContext();
        //timestamp.serialize(streamSerializationContext);
        //byte []otsAfter = streamSerializationContext.getOutput();
        //if (!Arrays.equals(ots, otsAfter)) {
        //}

    }


    @After
    public void tearDown() {
        executor.shutdown();
    }

}
