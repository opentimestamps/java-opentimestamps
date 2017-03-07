package com.eternitywall.http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

public class Request implements Callable<Response> {
    private URL url;
    private HttpURLConnection urlConnection;
    private Boolean isPost=false;
    private String params="";

    public Request(URL url) {
        this.url = url;
        try {
            urlConnection = (HttpURLConnection) this.url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addHeader(String key, String value){
        if(urlConnection != null) {
            urlConnection.setRequestProperty(key, value);
        }
    }

    public void doPost(String params){
        this.isPost=true;
        this.params=params;
    }

    @Override
    public Response call() throws Exception {

        urlConnection.setRequestProperty("Accept", "application/vnd.opentimestamps.v1");
        urlConnection.setRequestProperty("User-Agent", "java-opentimestamps");
        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConnection.setUseCaches(false);
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);

        if(this.isPost==true) {
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Length", "" + Integer.toString(this.params.length()));
            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            wr.writeBytes(this.params);
            wr.flush();
            wr.close();
        }

        InputStream is = urlConnection.getInputStream();
        return new Response(is);
    }
}