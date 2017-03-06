package com.eternitywall.op;

import com.eternitywall.Utils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by casatta on 06/03/17.
 */
public class TestOps {

    @Test
    public void testKeccak() {
        byte[] result = new OpKECCAK256().call(Utils.hexToBytes(""));
        assertEquals("c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470", Utils.bytesToHex(result));

        byte[] result2 = new OpKECCAK256().call(Utils.hexToBytes("80"));
        assertEquals("56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421", Utils.bytesToHex(result2));


    }
}
