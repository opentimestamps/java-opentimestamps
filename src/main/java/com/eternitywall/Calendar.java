package com.eternitywall;
/**
 * com.eternitywall.Calendar module.
 *
 * @module com.eternitywall.Calendar
 * @author EternityWall
 * @license LPGL3
 */


import java.net.*;
import java.io.*;
import java.util.logging.Logger;

/** Class representing Remote com.eternitywall.Calendar server interface */
public class Calendar {

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
            InputStream inputStream = con.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            byte[] byteArray = new byte[bufferedInputStream.available()];
            int current = bufferedInputStream.read(byteArray);
            while (current != -1) {
                byte[] buffer = new byte[bufferedInputStream.available()];
                current = bufferedInputStream.read(buffer);
                Utils.arraysConcat(byteArray, buffer);
            }

            // Response Hanlder
            byte[] body = byteArray;
            if (body.length > 10000) {
                log.severe("com.eternitywall.Calendar response exceeded size limit");
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
        }
    }


    /**
     * Get a timestamp for a given commitment.
     * @param {byte[]} commitment - The digest hash to send.
     */
    public Timestamp getTimestamp(byte[] commitment) {
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
            InputStream inputStream = con.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            byte[] byteArray = new byte[bufferedInputStream.available()];
            int current = bufferedInputStream.read(byteArray);
            while (current != -1) {
                byte[] buffer = new byte[bufferedInputStream.available()];
                current = bufferedInputStream.read(buffer);
                Utils.arraysConcat(byteArray, buffer);
            }

            // Response Hanlder
            byte[] body = byteArray;
            if (body.length > 10000) {
                log.severe("com.eternitywall.Calendar response exceeded size limit");
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
        }
    }
}
