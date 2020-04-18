package com.eternitywall;

import com.eternitywall.ots.BlockHeader;
import com.eternitywall.ots.Esplora;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestEsplora {

    @Test
    public void testEsplora() throws Exception {
        String s = Esplora.blockHash(0);
        assertEquals("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f", s);
        BlockHeader blockHeader = Esplora.block(s);
        assertEquals("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f", blockHeader.getBlockHash());
        assertEquals("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b", blockHeader.getMerkleroot());
    }
}
