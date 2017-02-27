package com.eternitywall;
/**
 * com.eternitywall.Calendar module.
 * @module com.eternitywall.Calendar
 * @author EternityWall
 * @license LPGL3
 */

import com.sun.xml.internal.ws.util.ByteArrayBuffer;

import java.net.*;
import java.io.*;

/** Class representing Remote com.eternitywall.Calendar server interface */
public class Calendar {

    String url;

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
        ByteArrayBuffer byteArrayBuffer = null;
        try {

            URL obj = new URL(url + "/digest");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");

            //add request header
            con.setRequestProperty("Accept", "application/vnd.opentimestamps.v1");
            con.setRequestProperty("User-Agent", "java-opentimestamps");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(new String(digest));
            wr.flush();
            wr.close();

            // Response
            int responseCode = con.getResponseCode();
            InputStream inputStream =con.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            byteArrayBuffer = new ByteArrayBuffer();
            int current;
            while ((current = bufferedInputStream.read()) != -1) {
                byteArrayBuffer.write((byte) current);
            }
            byteArrayBuffer.close();

            // Response Hanlder
            byte[] body = byteArrayBuffer.getRawData();
            if (body.length > 10000) {
                System.err.print("com.eternitywall.Calendar response exceeded size limit");
                return null;
            }

            StreamDeserializationContext ctx = new StreamDeserializationContext(body);
            Timestamp timestamp = Timestamp.deserialize(ctx, digest);
            return timestamp;

        } catch (ProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                byteArrayBuffer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Get a timestamp for a given commitment.
     * @param {byte[]} commitment - The digest hash to send.
     */
    public Timestamp getTimestamp(byte[] commitment) {
        ByteArrayBuffer byteArrayBuffer = null;
        try {

            URL obj = new URL(url + "/timestamp/" + Utils.bytesToHex(commitment));
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("Accept", "application/vnd.opentimestamps.v1");
            con.setRequestProperty("User-Agent", "java-opentimestamps");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Response
            int responseCode = con.getResponseCode();
            InputStream inputStream =con.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            byteArrayBuffer = new ByteArrayBuffer();
            int current;
            while ((current = bufferedInputStream.read()) != -1) {
                byteArrayBuffer.write((byte) current);
            }
            byteArrayBuffer.close();

            // Response Hanlder
            byte[] body = byteArrayBuffer.getRawData();
            if (body.length > 10000) {
                System.err.print("com.eternitywall.Calendar response exceeded size limit");
                return null;
            }

            StreamDeserializationContext ctx = new StreamDeserializationContext(body);
            Timestamp timestamp = Timestamp.deserialize(ctx, commitment);
            return timestamp;

        } catch (ProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                byteArrayBuffer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
