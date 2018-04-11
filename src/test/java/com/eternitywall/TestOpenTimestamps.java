package com.eternitywall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.eternitywall.http.Request;
import com.eternitywall.http.Response;
import com.eternitywall.ots.DetachedTimestampFile;
import com.eternitywall.ots.Hash;
import com.eternitywall.ots.MultiInsight;
import com.eternitywall.ots.OpenTimestamps;
import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;
import com.eternitywall.ots.Timestamp;
import com.eternitywall.ots.Utils;
import com.eternitywall.ots.attestation.TimeAttestation;
import com.eternitywall.ots.op.OpSHA256;

/**
 * Created by casatta on 28/02/17.
 */
public class TestOpenTimestamps{
    private ExecutorService executor;
    private byte[] incomplete;
    private byte[] incompleteOts;
    private String incompleteOtsInfo;
    private byte[] helloworld;
    private byte[] helloworldOts;
    private byte[] merkle1Ots;
    private byte[] merkle2Ots;
    private String merkle2OtsInfo;
    private byte[] merkle3Ots;
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
        Future<Response> merkle1OtsFuture = executor.submit(new Request(new URL( baseUrl + "/examples/merkle1.txt.ots")));
        Future<Response> merkle2OtsFuture = executor.submit(new Request(new URL( baseUrl + "/examples/merkle2.txt.ots")));
        Future<Response> merkle2OtsInfoFuture = executor.submit(new Request(new URL( baseUrl + "/examples/merkle2.txt.ots.info")));
        Future<Response> merkle3OtsFuture = executor.submit(new Request(new URL( baseUrl + "/examples/merkle3.txt.ots")));
        Future<Response> differentBlockchainOtsFuture = executor.submit(new Request(new URL( baseUrl + "/examples/different-blockchains.txt.ots")));
        Future<Response> differentBlockchainOtsInfoFuture = executor.submit(new Request(new URL( baseUrl + "/examples/different-blockchains.txt.ots.info")));

        incompleteOts = incompleteOtsFuture.get().getBytes();
        incomplete = incompleteFuture.get().getBytes();
        incompleteOtsInfo = incompleteOtsInfoFuture.get().getString();
        helloworld = helloworldFuture.get().getBytes();
        helloworldOts = helloworldOtsFuture.get().getBytes();
        merkle1Ots = merkle1OtsFuture.get().getBytes();
        merkle2Ots = merkle2OtsFuture.get().getBytes();
        merkle2OtsInfo = merkle2OtsInfoFuture.get().getString();
        merkle3Ots = merkle3OtsFuture.get().getBytes();
        differentBlockchainOts = differentBlockchainOtsFuture.get().getBytes();
        differentBlockchainOtsInfo = differentBlockchainOtsInfoFuture.get().getString();
    }

    @Test
    public void info() {
        String result = OpenTimestamps.info(DetachedTimestampFile.deserialize(incompleteOts));
        assertNotNull(result);
        assertNotNull(incompleteOtsInfo);
        boolean equals = result.equals(incompleteOtsInfo);
        assertTrue(equals);
        assertEquals(incompleteOtsInfo, result);

        String result2 = OpenTimestamps.info(DetachedTimestampFile.deserialize(merkle2Ots));
        assertNotNull(result2);
        assertNotNull(merkle2OtsInfo);
        assertEquals(merkle2OtsInfo, result2);

        String result3 = OpenTimestamps.info(DetachedTimestampFile.deserialize(differentBlockchainOts));
        System.err.println(result3);
        assertNotNull(result3);
        assertNotNull(differentBlockchainOtsInfo);
        //assertEquals(differentBlockchainOtsInfo, result3);
    }


    @Test
    public void stamp() throws NoSuchAlgorithmException, IOException {
        {
            byte[] bytes = Utils.randBytes(32);
            DetachedTimestampFile detached = DetachedTimestampFile.from(new Hash(bytes, OpSHA256._TAG));
            OpenTimestamps.stamp(detached);
            byte[] digest = detached.fileDigest();
            assertTrue(Arrays.equals(digest, bytes));
        }

        {
            DetachedTimestampFile detached = DetachedTimestampFile.from(Hash.from(helloworld, OpSHA256._TAG));
            OpenTimestamps.stamp(detached);
            byte[] digest = detached.fileDigest();
            assertTrue(Arrays.equals(digest, Utils.hexToBytes(helloWorldHashHex)));
        }

    }

    @Test
    public void merkle() throws NoSuchAlgorithmException, IOException {

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
    public void verify() throws NoSuchAlgorithmException, IOException {

        {
            DetachedTimestampFile detachedOts = DetachedTimestampFile.deserialize(helloworldOts);
            DetachedTimestampFile detached = DetachedTimestampFile.from(Hash.from(helloworld, OpSHA256._TAG));
            Long timestamp = OpenTimestamps.verify(detachedOts, detached);
            assertEquals(1432827678L, timestamp.longValue());
        }

        // verify on python call upgrade
        {
            DetachedTimestampFile detachedOts = DetachedTimestampFile.deserialize(incompleteOts);
            DetachedTimestampFile detached = DetachedTimestampFile.from(Hash.from(incomplete, OpSHA256._TAG));
            Long timestamp = OpenTimestamps.verify(detachedOts, detached);
            assertEquals(null, timestamp);
        }

    }

    @Test
    public void upgrade() throws IOException, NoSuchAlgorithmException {

        {
            DetachedTimestampFile detached = DetachedTimestampFile.from(Hash.from(incomplete, OpSHA256._TAG));
            DetachedTimestampFile detachedOts = DetachedTimestampFile.deserialize(incompleteOts);
            boolean changed= OpenTimestamps.upgrade(detachedOts);
            assertTrue(changed);
            Long timestamp = OpenTimestamps.verify(detachedOts, detached);
            assertEquals(1473227803L, timestamp.longValue());
        }

        {
            byte[] hashBytes = Utils.randBytes(32);
            DetachedTimestampFile detached = DetachedTimestampFile.from(Hash.from(hashBytes, OpSHA256._TAG));
            Timestamp stamp = OpenTimestamps.stamp(detached);
            boolean changed = OpenTimestamps.upgrade(stamp);
            assertFalse(changed);
        }
        //byte[] upgrade = OpenTimestamps.upgrade(stamp);
        //assertTrue( Arrays.equals(stamp,upgrade) );   //FIXME this doesn't work, should it?

    }

    @Test
    public void test() {

        byte []ots = Utils.hexToBytes("F0105C3F2B3F8524A32854E07AD8ADDE9C1908F10458D95A36F008088D287213A8B9880083DFE30D2EF90C8E2C2B68747470733A2F2F626F622E6274632E63616C656E6461722E6F70656E74696D657374616D70732E6F7267");
        byte []digest= Utils.hexToBytes("7aa9273d2a50dbe0cc5a6ccc444a5ca90c9491dd2ac91849e45195ae46f64fe352c3a63ba02775642c96131df39b5b85");
        Logger log = Logger.getLogger(MultiInsight.class.getName());
        //log.info("ots hex: " + Utils.bytesToHex(ots));

        StreamDeserializationContext streamDeserializationContext = new StreamDeserializationContext(ots);
        Timestamp timestamp = Timestamp.deserialize(streamDeserializationContext, digest);
        //log.info(Timestamp.strTreeExtended(timestamp,2));

        StreamSerializationContext streamSerializationContext = new StreamSerializationContext();
        timestamp.serialize(streamSerializationContext);
        byte []otsBefore = streamSerializationContext.getOutput();
        //log.info("fullOts hex: " + Utils.bytesToHex(otsBefore));

        //log.info("upgrading " + OpenTimestamps.info(timestamp));
        boolean changed = OpenTimestamps.upgrade(timestamp);
        assertTrue(changed);

        //streamSerializationContext = new StreamSerializationContext();
        //timestamp.serialize(streamSerializationContext);
        //byte []otsAfter = streamSerializationContext.getOutput();
        //if (!Arrays.equals(ots, otsAfter)) {
        //}

    }

    @Test
    public void shrink() throws Exception {
        {
            DetachedTimestampFile detached = DetachedTimestampFile.deserialize(helloworldOts);
            Timestamp timestamp = detached.getTimestamp();

            assertEquals(timestamp.getAttestations().size(), 1);
            TimeAttestation resultAttestation = timestamp.shrink();
            assertEquals(timestamp.getAttestations().size(), 1);
            assertTrue(timestamp.getAttestations().contains(resultAttestation));
        }

        {
            DetachedTimestampFile detached = DetachedTimestampFile.deserialize(incompleteOts);
            Timestamp timestamp = detached.getTimestamp();

            assertEquals(timestamp.getAttestations().size(), 1);
            TimeAttestation resultAttestation = timestamp.shrink();
            assertEquals(timestamp.getAttestations().size(), 1);
            assertTrue(timestamp.getAttestations().contains(resultAttestation));

            OpenTimestamps.upgrade(detached);
            assertEquals(timestamp.getAttestations().size(), 2);
            TimeAttestation resultAttestationBitcoin = timestamp.shrink();
            assertEquals(timestamp.getAttestations().size(), 2);
            assertTrue(timestamp.getAttestations().contains(resultAttestationBitcoin));
        }

        {
            DetachedTimestampFile detached = DetachedTimestampFile.deserialize(merkle1Ots);
            Timestamp timestamp = detached.getTimestamp();

            assertEquals(timestamp.getAttestations().size(), 2);
            TimeAttestation resultAttestation = timestamp.shrink();
            assertEquals(timestamp.getAttestations().size(), 2);
            assertTrue(timestamp.getAttestations().contains(resultAttestation));

            OpenTimestamps.upgrade(detached);
            assertEquals(timestamp.getAttestations().size(), 3);
            TimeAttestation resultAttestationBitcoin = timestamp.shrink();
            assertEquals(timestamp.getAttestations().size(), 2);
            assertTrue(timestamp.getAttestations().contains(resultAttestationBitcoin));

        }

        {
            DetachedTimestampFile detached = DetachedTimestampFile.deserialize(merkle2Ots);
            Timestamp timestamp = detached.getTimestamp();

            assertEquals(timestamp.getAttestations().size(), 2);
            TimeAttestation resultAttestation = timestamp.shrink();
            assertEquals(timestamp.getAttestations().size(), 2);
            assertTrue(timestamp.getAttestations().contains(resultAttestation));

            OpenTimestamps.upgrade(detached);
            assertEquals(timestamp.getAttestations().size(), 3);
            TimeAttestation resultAttestationBitcoin = timestamp.shrink();
            assertEquals(timestamp.getAttestations().size(), 2);
            assertTrue(timestamp.getAttestations().contains(resultAttestationBitcoin));
        }

        {
            DetachedTimestampFile detached = DetachedTimestampFile.deserialize(merkle3Ots);
            Timestamp timestamp = detached.getTimestamp();

            assertEquals(timestamp.getAttestations().size(), 2);
            TimeAttestation resultAttestation = timestamp.shrink();
            assertEquals(timestamp.getAttestations().size(), 2);
            assertTrue(timestamp.getAttestations().contains(resultAttestation));

            OpenTimestamps.upgrade(detached);
            assertEquals(timestamp.getAttestations().size(), 3);
            TimeAttestation resultAttestationBitcoin = timestamp.shrink();
            assertEquals(timestamp.getAttestations().size(), 2);
            assertTrue(timestamp.getAttestations().contains(resultAttestationBitcoin));
        }

    }

    @After
    public void tearDown() {
        executor.shutdown();
    }

}
