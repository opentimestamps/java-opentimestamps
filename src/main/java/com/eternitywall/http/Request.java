package com.eternitywall.http;

import com.eternitywall.MultiInsight;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class Request implements Callable<Response> {
    private static Logger log = Logger.getLogger(MultiInsight.class.getName());

    private URL url;
    private byte[] data;
    private Map<String,String> headers;
    private BlockingQueue<Response> queue;

    public Request(URL url) {
        this.url = url;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setQueue(BlockingQueue<Response> queue) {
        this.queue = queue;
    }

    /*
        urlConnection.setRequestProperty("Accept", "application/vnd.opentimestamps.v1");
        urlConnection.setRequestProperty("User-Agent", "java-opentimestamps");
        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    */

    @Override
    public Response call() throws Exception {

        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setRequestProperty("User-Agent", "java");
            if(headers!=null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            if (data != null) {
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Length", "" + Integer.toString(this.data.length));
                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.write(this.data, 0, this.data.length);
                wr.flush();
                wr.close();
            } else {
                httpURLConnection.setRequestMethod("GET");
            }

            InputStream is = httpURLConnection.getInputStream();
            Response response = new Response(is);

            if(queue!=null) {
                response.setFromUrl(url.toString());
                queue.offer(response);
            }
            return response;

        }catch (Exception e) {
            log.fine(url.toString() + " exception " + e);
            if(queue!=null) {
                queue.offer(new Response()); //FIXME

            }
        }
        return null;
    }

    public static String urlEncodeUTF8(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
    }
    public static String urlEncodeUTF8(Map<?,?> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?,?> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s",
                    urlEncodeUTF8(entry.getKey().toString()),
                    urlEncodeUTF8(entry.getValue().toString())
            ));
        }
        return sb.toString();
    }
}