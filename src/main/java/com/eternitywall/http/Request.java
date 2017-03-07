package com.eternitywall.http;

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

public class Request implements Callable<Response> {
    private URL url;
    private String data;
    private Map<String,String> headers;
    private BlockingQueue<Response> queue;

    public Request(URL url) {
        this.url = url;
    }

    public void setData(String data) {
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
            httpURLConnection.setReadTimeout(2000);
            httpURLConnection.setConnectTimeout(2000);

            if(headers!=null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            if (data != null) {
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Length", "" + Integer.toString(this.data.length()));
                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(this.data);
                wr.flush();
                wr.close();
            } else {
                httpURLConnection.setRequestMethod("GET");
            }

            InputStream is = httpURLConnection.getInputStream();
            Response response = new Response(is);

            if(queue!=null) {
                response.setFromUrl(url.getPath());
                queue.offer(response);
            }
            return response;

        }catch (Exception e) {
            System.out.println("exception " + e);
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