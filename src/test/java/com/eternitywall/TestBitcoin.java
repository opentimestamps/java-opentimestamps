package com.eternitywall;

import com.eternitywall.ots.BitcoinNode;
import com.eternitywall.ots.BlockHeader;
import com.eternitywall.ots.Timestamp;
import com.eternitywall.ots.Utils;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by casatta on 06/03/17.
 */
public class TestBitcoin {
    private static Logger log = Utils.getLogger(TestBitcoin.class.getName());


    @Test
    public void testBitcoin() throws Exception {
        try {
            Properties properties = BitcoinNode.readBitcoinConf();
            BitcoinNode bitcoin=new BitcoinNode(properties);
            String info = bitcoin.getInfo().toString();
            assertNotNull(info);
            JSONObject jsonObject=new JSONObject(info);
            assertNotNull(jsonObject);
            String s = bitcoin.getBlockHash(0);
            assertEquals("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f", s);
            BlockHeader blockHeader = bitcoin.getBlockHeader(s);
            assertEquals("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b", blockHeader.getMerkleroot());
            log.info(blockHeader.toString());
            assertEquals("1231006505", String.valueOf(blockHeader.getTime()));
        } catch (Exception e) {
            log.info("no bitcoin node");
        }

    }



}
