package com.eternitywall;
/**
 * com.eternitywall.Calendar module.
 *
 * @module com.eternitywall.Calendar
 * @author EternityWall
 * @license LPGL3
 */


import com.eternitywall.http.Request;
import com.eternitywall.http.Response;

import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/** Class representing Remote com.eternitywall.Calendar server interface */
public class Calendar{

    String url;
    private static Logger log = Logger.getLogger(Calendar.class.getName());

    /**
     * Create a RemoteCalendar.
     * @param {string} url - The server url.
     */
    Calendar(String url) {
        this.url = url;
    }

    /**
     * Submitting a digest to remote calendar. Returns a com.eternitywall.Timestamp committing to that digest.
     * @param {byte[]} digest - The digest hash to send.
     */
    public Timestamp submit(byte[] digest) {
        try {

            Map<String, String> headers = new HashMap<>();
            headers.put("Accept","application/vnd.opentimestamps.v1");
            headers.put("User-Agent","java-opentimestamps");
            headers.put("Content-Type","application/x-www-form-urlencoded");

            URL obj = new URL(url + "/digest");
            Request task = new Request(obj);
            task.setData(new String(digest));
            task.setHeaders(headers);
            Response response = task.call();
            byte[] body = response.getBytes();

            if (body.length > 10000) {
                log.severe("com.eternitywall.Calendar response exceeded size limit");
                return null;
            }

            StreamDeserializationContext ctx = new StreamDeserializationContext(body);
            Timestamp timestamp = Timestamp.deserialize(ctx, digest);
            return timestamp;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Get a timestamp for a given commitment.
     * @param {byte[]} commitment - The digest hash to send.
     */
    public Timestamp getTimestamp(byte[] commitment) {
        try {

            Map<String, String> headers = new HashMap<>();
            headers.put("Accept","application/vnd.opentimestamps.v1");
            headers.put("User-Agent","java-opentimestamps");
            headers.put("Content-Type","application/x-www-form-urlencoded");

            URL obj = new URL(url + "/timestamp/" + Utils.bytesToHex(commitment));
            Request task = new Request(obj);
            task.setHeaders(headers);
            Response response = task.call();
            byte[] body = response.getBytes();

            if (body.length > 10000) {
                log.severe("com.eternitywall.Calendar response exceeded size limit");
                return null;
            }

            StreamDeserializationContext ctx = new StreamDeserializationContext(body);
            Timestamp timestamp = Timestamp.deserialize(ctx, commitment);
            return timestamp;

        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
}
