package com.eternitywall;

import com.eternitywall.ots.Calendar;
import com.eternitywall.ots.CalendarAsyncSubmit;
import com.eternitywall.ots.Optional;
import com.eternitywall.ots.Timestamp;
import com.eternitywall.ots.Utils;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.junit.Test;

import java.io.FileInputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class TestCalendar {
    private static Logger log = Utils.getLogger(TestCalendar.class.getName());

    @Test
    public void testSingle() throws Exception {
        String calendarUrl = "https://finney.calendar.eternitywall.com";
        byte[] digest = Utils.randBytes(32);
        Calendar calendar = new Calendar(calendarUrl);
        Timestamp timestamp = calendar.submit(digest);
        assertNotNull(timestamp);
        assertArrayEquals(timestamp.getDigest(), digest);
    }

    @Test
    public void testPrivate() throws Exception {
        byte[] digest = Utils.randBytes(32);

        // key.wif it's a file of properties with the format
        // <calendar url> = <private key in wif format>
        // auth.calendar.eternitywall.com = KwT2r9sL........

        Path path = Paths.get("key.wif");

        if (!Files.exists(path)) {
            // No need to carry on this test
            return;
        }

        Properties properties = new Properties();
        properties.load(new FileInputStream("key.wif"));
        HashMap<String, String> privateUrls = new HashMap<>();

        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            privateUrls.put(key, value);
        }

        assertFalse(privateUrls.isEmpty());

        for (Map.Entry<String, String> entry : privateUrls.entrySet()) {
            String calendarUrl = "https://" + entry.getKey();
            String wifKey = entry.getValue();

            Calendar calendar = new Calendar(calendarUrl);
            ECKey key;

            try {
                BigInteger privKey = new BigInteger(wifKey);
                key = ECKey.fromPrivate(privKey);
            } catch (Exception e) {
                DumpedPrivateKey dumpedPrivateKey = new DumpedPrivateKey(NetworkParameters.prodNet(), wifKey);
                key = dumpedPrivateKey.getKey();
            }

            calendar.setKey(key);
            Timestamp timestamp = calendar.submit(digest);
            assertNotNull(timestamp);
            assertArrayEquals(timestamp.getDigest(), digest);
        }
    }

    @Test
    public void testPrivateWif() throws Exception {
        byte[] digest = Utils.randBytes(32);
        Path path = Paths.get("key.wif");

        if (!Files.exists(path)) {
            assertTrue(true);
            return;
        }

        Properties properties = new Properties();
        properties.load(new FileInputStream("key.wif"));
        HashMap<String, String> privateUrls = new HashMap<>();

        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            privateUrls.put(key, value);
        }

        assertFalse(privateUrls.isEmpty());

        for (Map.Entry<String, String> entry : privateUrls.entrySet()) {
            String calendarUrl = "https://" + entry.getKey();
            String wifKey = entry.getValue();

            Calendar calendar = new Calendar(calendarUrl);
            ECKey key;
            DumpedPrivateKey dumpedPrivateKey = new DumpedPrivateKey(NetworkParameters.prodNet(), wifKey);
            key = dumpedPrivateKey.getKey();
            calendar.setKey(key);
            Timestamp timestamp = calendar.submit(digest);
            assertNotNull(timestamp);
            assertArrayEquals(timestamp.getDigest(), digest);
        }
    }

    @Test
    public void testSingleAsync() throws Exception {
        String calendarUrl = "https://finney.calendar.eternitywall.com";
        byte[] digest = Utils.randBytes(32);
        ArrayBlockingQueue<Optional<Timestamp>> queue = new ArrayBlockingQueue<>(1);

        CalendarAsyncSubmit task = new CalendarAsyncSubmit(calendarUrl, digest);
        task.setQueue(queue);
        task.call();
        Optional<Timestamp> timestamp = queue.take();
        assertTrue(timestamp.isPresent());
        assertNotNull(timestamp.get());
        assertArrayEquals(timestamp.get().getDigest(), digest);
    }

    @Test
    public void testSingleAsyncPrivate() throws Exception {
        ArrayBlockingQueue<Optional<Timestamp>> queue = new ArrayBlockingQueue<>(1);
        byte[] digest = Utils.randBytes(32);
        Path path = Paths.get("signature.key");

        if (!Files.exists(path)) {
            assertTrue(true);
            return;
        }

        Properties properties = new Properties();
        properties.load(new FileInputStream("signature.key"));
        HashMap<String, String> privateUrls = new HashMap<>();

        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            privateUrls.put(key, value);
        }

        assertFalse(privateUrls.isEmpty());

        for (Map.Entry<String, String> entry : privateUrls.entrySet()) {
            String calendarUrl = "https://" + entry.getKey();
            String signature = entry.getValue();

            CalendarAsyncSubmit task = new CalendarAsyncSubmit(calendarUrl, digest);
            ECKey key;
            BigInteger privKey = new BigInteger(signature);
            key = ECKey.fromPrivate(privKey);
            task.setKey(key);
            task.setQueue(queue);
            task.call();
            Optional<Timestamp> timestamp = queue.take();
            assertTrue(timestamp.isPresent());
            assertNotNull(timestamp.get());
            assertArrayEquals(timestamp.get().getDigest(), digest);
        }
    }

    @Test
    public void testMulti() throws Exception {
        List<String> calendarsUrl = new ArrayList<String>();
        calendarsUrl.add("https://alice.btc.calendar.opentimestamps.org");
        calendarsUrl.add("https://bob.btc.calendar.opentimestamps.org");
        calendarsUrl.add("https://finney.calendar.eternitywall.com");
        byte[] digest = Utils.randBytes(32);
        ArrayBlockingQueue<Optional<Timestamp>> queue = new ArrayBlockingQueue<>(calendarsUrl.size());
        ExecutorService executor = Executors.newFixedThreadPool(calendarsUrl.size());
        int m = calendarsUrl.size();

        for (final String calendarUrl : calendarsUrl) {
            try {
                CalendarAsyncSubmit task = new CalendarAsyncSubmit(calendarUrl, digest);
                task.setQueue(queue);
                executor.submit(task);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int count = 0;

        for (final String calendarUrl : calendarsUrl) {
            try {
                Optional<Timestamp> stamp = queue.take();
                //timestamp.merge(stamp);

                if (stamp.isPresent()) {
                    count++;
                }

                if (count >= m) {
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (count < m) {
            log.severe("Failed to create timestamp: requested " + String.valueOf(m) + " attestation" + ((m > 1) ? "s" : "") + " but received only " + String.valueOf(count));
        }

        assertFalse(count < m);

        executor.shutdown();
    }

    @Test
    public void testMultiWithInvalidCalendar() throws Exception {
        List<String> calendarsUrl = new ArrayList<String>();
        calendarsUrl.add("https://alice.btc.calendar.opentimestamps.org");
        calendarsUrl.add("https://bob.btc.calendar.opentimestamps.org");
        calendarsUrl.add("");
        byte[] digest = Utils.randBytes(32);
        ArrayBlockingQueue<Optional<Timestamp>> queue = new ArrayBlockingQueue<>(calendarsUrl.size());
        ExecutorService executor = Executors.newFixedThreadPool(calendarsUrl.size());
        int m = 2;

        for (final String calendarUrl : calendarsUrl) {
            try {
                CalendarAsyncSubmit task = new CalendarAsyncSubmit(calendarUrl, digest);
                task.setQueue(queue);
                executor.submit(task);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int count = 0;

        for (final String calendarUrl : calendarsUrl) {
            try {
                Optional<Timestamp> stamp = queue.take();
                //timestamp.merge(stamp);

                if (stamp.isPresent()) {
                    count++;
                }

                if (count >= m) {
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (count < m) {
            log.severe("Failed to create timestamp: requested " + String.valueOf(m) + " attestation" + ((m > 1) ? "s" : "") + " but received only " + String.valueOf(count));
        }

        assertFalse(count < m);

        executor.shutdown();
    }

    @Test
    public void rfc6979() {
        BigInteger privateKey = new BigInteger("235236247357325473457345");
        ECKey ecKey = ECKey.fromPrivate(privateKey);
        String a = ecKey.signMessage("a");
        assertEquals("IBY7a75Ygps/o1BqTQ0OpFL+a8WHfd9jNO/8820ST0gyQ0SAuIWKm8/M90aG1G40oJvjrlcoiKngKAYYsJS6I0s=", a);
    }
}
