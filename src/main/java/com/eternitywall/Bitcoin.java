package com.eternitywall;

import com.eternitywall.http.Request;
import com.eternitywall.http.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by casatta on 06/03/17.
 */
public class Bitcoin {

    private String authString;
    private String urlString;

    private Bitcoin() {
    }

    public Bitcoin(Properties bitcoinConf) {
        authString = String.valueOf(Base64Coder.encode( String.format("%s:%s",bitcoinConf.getProperty("rpcuser"), bitcoinConf.getProperty("rpcpassword")).getBytes()));
        urlString = String.format("http://%s:%s", bitcoinConf.getProperty("rpcconnect"), bitcoinConf.getProperty("rpcport"));
    }

    public static Properties readBitcoinConf() {
        String home = System.getProperty("user.home");

        List<String> list= Arrays.asList("/.bitcoin/bitcoin.conf", "\\AppData\\Roaming\\Bitcoin\\bitcoin.conf", "/Library/Application Support/Bitcoin/bitcoin.conf");
        for(String dir : list) {
            Properties prop = new Properties();
            InputStream input = null;

            try {

                input = new FileInputStream(home+dir);

                // load a properties file
                prop.load(input);

                // get the property value and print it out
                if(prop.getProperty("rpcuser")!=null && prop.getProperty("rpcpassword")!=null) {
                    if(prop.getProperty("rpcconnect")==null)
                        prop.setProperty("rpcconnect","127.0.0.1");
                    if(prop.getProperty("rpcport")==null)
                        prop.setProperty("rpcport","8332");
                    return prop;
                }

            } catch (IOException ex) {
                //ex.printStackTrace();
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public JSONObject getInfo() {
        JSONObject json = new JSONObject();
        json.put("id", "java");
        json.put("method", "getinfo");
        return callRPC(json);
    }

    public JSONObject getBlockHeader(Integer height) {
        return getBlockHeader(getBlockHash(height));
    }

    public JSONObject getBlockHeader(String hash) {
        JSONObject json = new JSONObject();
        json.put("id", "java");
        json.put("method", "getblockheader");
        JSONArray array=new JSONArray();
        array.put(hash);
        json.put("params", array);
        return callRPC(json);
    }

    public String getBlockHash(Integer height) {
        JSONObject json = new JSONObject();
        json.put("id", "java");
        json.put("method", "getblockhash");
        JSONArray array=new JSONArray();
        array.put(height);
        json.put("params", array);
        return callRPC(json).getString("result");
    }

    private JSONObject callRPC(JSONObject query) {
        try {
            String s=query.toString();
            URL url = new URL(urlString);
            Request request = new Request(url);
            Map<String,String> headers = new HashMap<>();
            headers.put("Authorization", "Basic " + authString);
            request.setHeaders(headers);
            request.setData(s);
            Response response = null;  //sync call
            try {
                response = request.call();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new JSONObject(response.getString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
