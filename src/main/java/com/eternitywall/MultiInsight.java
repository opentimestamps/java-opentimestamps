package com.eternitywall;

import com.eternitywall.http.Request;
import com.eternitywall.http.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by luca on 07/03/2017.
 */
public class MultiInsight {


    private ExecutorService executor;
    private List<String> insightUrls;
    private List<Future> futures;

    public MultiInsight(){
        insightUrls = new ArrayList<>();
        insightUrls.add("https://search.bitaccess.com/insight-api");
        insightUrls.add("https://chain.localbitcoins.com/api");
        insightUrls.add("https://insight.bitpay.com/api");
    }


    /**
     * Retrieve the block information from the block hash.
     * @param {string} height - Height of the block.
     */
    public BlockHeader block(String hash) throws Exception {
        executor = Executors.newFixedThreadPool(insightUrls.size());

        try {

            futures = new ArrayList<>();
            for (String insightUrl : insightUrls) {
                URL url = new URL(insightUrl + "/block/" + hash);
                Future<Response> future = executor.submit(new Request(url));
                futures.add(future);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new Exception();
        }


        HashSet<BlockHeader> blockHeaders = new HashSet<>();

        for (Future<Response> future : futures) {
            try {

                JSONObject jsonObject = future.get().getJson();
                String merkleroot = jsonObject.getString("merkleroot");
                String time = String.valueOf(jsonObject.getInt("time"));
                BlockHeader blockHeader = new BlockHeader();
                blockHeader.setMerkleroot(merkleroot);
                blockHeader.setTime(time);
                blockHeaders.add(blockHeader);

                if (blockHeaders.contains(blockHeader)) {
                    return blockHeader;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new Exception();
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw new Exception();
            } catch (JSONException e) {
                e.printStackTrace();
                throw new Exception();
            } catch (IOException e) {
                e.printStackTrace();
                throw new Exception();
            }
        }
        throw new Exception();
    }

    /**
     * Retrieve the block hash from the block height.
     * @param {string} height - Height of the block.
     */
    public BlockHeader blockhash(String height) throws Exception {
        executor = Executors.newFixedThreadPool(insightUrls.size());

        try {

            futures = new ArrayList<>();
            for (String insightUrl : insightUrls) {
                URL url = new URL(insightUrl + "/block-index/" + height);
                Future<Response> future = executor.submit(new Request(url));
                futures.add(future);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new Exception();
        }


        HashSet<BlockHeader> blockHeaders = new HashSet<>();
        try {

            for (Future<Response> future : futures) {
                JSONObject jsonObject = future.get().getJson();
                String merkleroot = jsonObject.getString("merkleroot");
                String time = String.valueOf(jsonObject.getInt("time"));
                BlockHeader blockHeader = new BlockHeader();
                blockHeader.setMerkleroot(merkleroot);
                blockHeader.setTime(time);
                blockHeaders.add(blockHeader);

                if (blockHeaders.contains(blockHeader)){
                    return blockHeader;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new Exception();
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new Exception();
        } catch (JSONException e) {
            e.printStackTrace();
            throw new Exception();
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception();
        }

        throw new Exception();
    }
}
