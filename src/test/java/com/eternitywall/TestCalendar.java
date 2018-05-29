package com.eternitywall;

import com.eternitywall.ots.*;
import com.eternitywall.ots.Calendar;
import com.eternitywall.ots.Optional;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.junit.Test;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestCalendar {

    private static Logger log = Utils.getLogger(TestCalendar.class.getName());


    @Test
    public void TestSingle() throws Exception {
        String calendarUrl = "https://finney.calendar.eternitywall.com";
        byte[] digest = Utils.randBytes(32);
        Calendar calendar = new Calendar(calendarUrl);
        Timestamp timestamp = calendar.submit(digest);
        assertTrue(timestamp != null);
        assertTrue(Arrays.equals(timestamp.getDigest() , digest));
    }




    @Test
    public void TestPrivate() throws Exception {
        byte[] digest = Utils.randBytes(32);

        // key.wif it's a file of properties with the format
        // <calendar url> = <private key in wif format>
        // auth.calendar.eternitywall.com = KwT2r9sL........

        Path path = Paths.get("key.wif");
        if(!Files.exists(path)){
            assertTrue(true);
            return;
        }

        Properties properties = new Properties();
        properties.load(new FileInputStream("key.wif"));
        HashMap<String,String> privateUrls = new HashMap<>();
        for(String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            privateUrls.put(key,value);
        }
        assertFalse(privateUrls.size() == 0);

        for(Map.Entry<String, String> entry : privateUrls.entrySet()) {
            String calendarUrl = "https://"+entry.getKey();
            String wifKey = entry.getValue();

            Calendar calendar = new Calendar(calendarUrl);
            ECKey key;
            try {
                BigInteger privKey = new BigInteger(wifKey);
                key = ECKey.fromPrivate(privKey);
            }catch (Exception e){
                DumpedPrivateKey dumpedPrivateKey = new DumpedPrivateKey(NetworkParameters.prodNet(), wifKey);
                key = dumpedPrivateKey.getKey();
            }
            calendar.setKey(key);
            Timestamp timestamp = calendar.submit(digest);
            assertTrue(timestamp != null);
            assertTrue(Arrays.equals(timestamp.getDigest() , digest));
        }

    }

    @Test
    public void TestPrivateWif() throws Exception {
        byte[] digest = Utils.randBytes(32);
        Path path = Paths.get("key.wif");
        if(!Files.exists(path)){
            assertTrue(true);
            return;
        }

        Properties properties = new Properties();
        properties.load(new FileInputStream("key.wif"));
        HashMap<String,String> privateUrls = new HashMap<>();
        for(String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            privateUrls.put(key,value);
        }
        assertFalse(privateUrls.size() == 0);

        for(Map.Entry<String, String> entry : privateUrls.entrySet()) {
            String calendarUrl = "https://"+entry.getKey();
            String wifKey = entry.getValue();

            Calendar calendar = new Calendar(calendarUrl);
            ECKey key;
            DumpedPrivateKey dumpedPrivateKey = new DumpedPrivateKey(NetworkParameters.prodNet(), wifKey);
            key = dumpedPrivateKey.getKey();
            calendar.setKey(key);
            Timestamp timestamp = calendar.submit(digest);
            assertTrue(timestamp != null);
            assertTrue(Arrays.equals(timestamp.getDigest() , digest));
        }

    }

    @Test
    public void TestSingleAsync() throws Exception {
        String calendarUrl = "https://finney.calendar.eternitywall.com";
        byte[] digest = Utils.randBytes(32);
        ArrayBlockingQueue<Optional<Timestamp>> queue = new ArrayBlockingQueue<>(1);

        CalendarAsyncSubmit task = new CalendarAsyncSubmit(calendarUrl, digest);
        task.setQueue(queue);
        task.call();
        Optional<Timestamp> timestamp = queue.take();
        assertTrue(timestamp.isPresent());
        assertTrue(timestamp.get() != null);
        assertTrue(Arrays.equals(timestamp.get().getDigest() , digest));
    }

    @Test
    public void TestSingleAsyncPrivate() throws Exception {

        ArrayBlockingQueue<Optional<Timestamp>> queue = new ArrayBlockingQueue<>(1);
        byte[] digest = Utils.randBytes(32);
        Path path = Paths.get("signature.key");
        if(!Files.exists(path)){
            assertTrue(true);
            return;
        }

        Properties properties = new Properties();
        properties.load(new FileInputStream("signature.key"));
        HashMap<String,String> privateUrls = new HashMap<>();
        for(String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            privateUrls.put(key,value);
        }
        assertFalse(privateUrls.size() == 0);

        for(Map.Entry<String, String> entry : privateUrls.entrySet()) {
            String calendarUrl = "https://"+entry.getKey();
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
            assertTrue(timestamp.get() != null);
            assertTrue(Arrays.equals(timestamp.get().getDigest() , digest));
        }


    }

    @Test
    public void TestMulti() throws Exception {

        List<String> calendarsUrl = new ArrayList<String>();
        calendarsUrl.add("https://alice.btc.calendar.opentimestamps.org");
        calendarsUrl.add("https://bob.btc.calendar.opentimestamps.org");
        calendarsUrl.add("https://finney.calendar.eternitywall.com");
        byte[] digest = Utils.randBytes(32);
        ArrayBlockingQueue<Optional<Timestamp>> queue = new ArrayBlockingQueue<>(calendarsUrl.size());
        ExecutorService executor = Executors.newFixedThreadPool(calendarsUrl.size());
        int m=calendarsUrl.size();

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
                if(stamp.isPresent()) {
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

        //shut down the executor service now
        executor.shutdown();
    }



    @Test
    public void TestMultiWithInvalidCalendar() throws Exception {

        List<String> calendarsUrl = new ArrayList<String>();
        calendarsUrl.add("https://alice.btc.calendar.opentimestamps.org");
        calendarsUrl.add("https://bob.btc.calendar.opentimestamps.org");
        calendarsUrl.add("");
        byte[] digest = Utils.randBytes(32);
        ArrayBlockingQueue<Optional<Timestamp>> queue = new ArrayBlockingQueue<>(calendarsUrl.size());
        ExecutorService executor = Executors.newFixedThreadPool(calendarsUrl.size());
        int m=2;

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
                if(stamp.isPresent()) {
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

        //shut down the executor service now
        executor.shutdown();
    }


    @Test
    public void rfc6979() {
        BigInteger privKey = new BigInteger("235236247357325473457345");
        ECKey ecKey = ECKey.fromPrivate(privKey);
        String a = ecKey.signMessage("a");
        System.out.println(a);
        assertTrue(a.equals("IBY7a75Ygps/o1BqTQ0OpFL+a8WHfd9jNO/8820ST0gyQ0SAuIWKm8/M90aG1G40oJvjrlcoiKngKAYYsJS6I0s="));
    }

}
