package com.eternitywall;

import com.eternitywall.http.Request;
import com.eternitywall.http.Response;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by luca on 07/03/2017.
 */
public class MultiInsight {


    private ExecutorService executor;
    private List<String> insightUrls;
    private BlockingQueue<Response> queueBlockHeader;
    private BlockingQueue<Response> queueBlockHash;

    public MultiInsight(){
        insightUrls = new ArrayList<>();
        insightUrls.add("https://search.bitaccess.com/insight-api");
        insightUrls.add("https://www.localbitcoinschain.com/api");
        insightUrls.add("https://insight.bitpay.com/api");
        queueBlockHeader = new ArrayBlockingQueue<>(insightUrls.size());
        queueBlockHash = new ArrayBlockingQueue<>(insightUrls.size());
        executor = Executors.newFixedThreadPool(insightUrls.size());
    }


    /**
     * Retrieve the block information from the block hash.
     * @param {string} height - Height of the block.
     */
    public BlockHeader block(String hash) throws Exception {
        for (String insightUrl: insightUrls) {
            URL url = new URL(insightUrl + "/block/" + hash);
            Request task = new Request(url);
            task.setQueue(queueBlockHeader);
            executor.submit(task);
        }
        Set<BlockHeader> results = new HashSet<>();

        for (int i = 0; i < insightUrls.size(); i++) {
            Response take = queueBlockHeader.take();
            JSONObject jsonObject = take.getJson();
            String merkleroot = jsonObject.getString("merkleroot");
            String time = String.valueOf(jsonObject.getInt("time"));
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setMerkleroot(merkleroot);
            blockHeader.setTime(time);

            if (results.contains(blockHeader)){
                return blockHeader;
            }
            results.add(blockHeader);
        }

        return null;
    }

    /**
     * Retrieve the block hash from the block height.
     * @param {string} height - Height of the block.
     */
    public String blockHash(String height) throws Exception {
        for (String insightUrl: insightUrls) {
            URL url = new URL(insightUrl + "/block-index/" + height);
            Request task = new Request(url);
            task.setQueue(queueBlockHash);
            executor.submit(task);
        }
        Set<String> results = new HashSet<>();

        for (int i = 0; i < insightUrls.size(); i++) {
            Response take = queueBlockHash.take();
            if(take.isValid()) {
                JSONObject jsonObject = take.getJson();
                String blockHash = jsonObject.getString("blockHash");
                System.out.println(take.getFromUrl() + ":" + blockHash);

                if (results.contains(blockHash)) {
                    return blockHash;
                }
                results.add(blockHash);
            }
        }

        return null;
    }

}
