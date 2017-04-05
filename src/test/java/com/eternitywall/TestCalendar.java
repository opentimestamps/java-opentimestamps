package com.eternitywall;

import com.eternitywall.ots.Calendar;
import com.eternitywall.ots.CalendarAsyncSubmit;
import com.eternitywall.ots.Optional;
import com.eternitywall.ots.Timestamp;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by luca on 05/04/2017.
 */
public class TestCalendar {

    private static Logger log = Logger.getLogger(TestCalendar.class.getName());


    @Test
    public void TestSingle() throws Exception {
        String calendarUrl = "https://ots.eternitywall.it";
        byte[] digest = DatatypeConverter.parseHexBinary("57cfa5c46716df9bd9e83595bce439c58108d8fcc1678f30d4c6731c3f1fa6c79ed712c66fb1ac8d4e4eb0e7");
        Calendar calendar = new Calendar(calendarUrl);
        Timestamp timestamp = calendar.submit(digest);
        assertTrue(timestamp != null);
        assertTrue(Arrays.equals(timestamp.getDigest() , digest));
    }

    @Test
    public void TestSingleAsync() throws Exception {
        String calendarUrl = "https://ots.eternitywall.it";
        byte[] digest = DatatypeConverter.parseHexBinary("57cfa5c46716df9bd9e83595bce439c58108d8fcc1678f30d4c6731c3f1fa6c79ed712c66fb1ac8d4e4eb0e7");
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
    public void TestMulti() throws Exception {

        List<String> calendarsUrl = new ArrayList<String>();
        calendarsUrl.add("https://alice.btc.calendar.opentimestamps.org");
        calendarsUrl.add("https://bob.btc.calendar.opentimestamps.org");
        calendarsUrl.add("https://ots.eternitywall.it");
        byte[] digest = DatatypeConverter.parseHexBinary("57cfa5c46716df9bd9e83595bce439c58108d8fcc1678f30d4c6731c3f1fa6c79ed712c66fb1ac8d4e4eb0e7");
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
                count++;
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


}
