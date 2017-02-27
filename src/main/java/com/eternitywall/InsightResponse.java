package com.eternitywall;

import java.text.SimpleDateFormat;

/**
 * Created by luca on 27/02/2017.
 */
public class InsightResponse {

    private String merkleroot;
    private String blockHash;
    private String time;

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public String getFormattedTime(){
        String formatted = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Long.valueOf(time)*1000);
        return formatted;

    }

    public String getMerkleroot() {
        return merkleroot;
    }

    public void setMerkleroot(String merkleroot) {
        this.merkleroot = merkleroot;
    }
    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }
}
