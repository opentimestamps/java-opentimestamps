package com.eternitywall;

import org.junit.Test;

/**
 * Created by casatta on 07/03/17.
 */
public class TestMultiInsight {

    @Test
    public void testMulti() throws Exception {
        MultiInsight multiInsight = new MultiInsight();
        String s = multiInsight.blockHash("0");
        System.out.println(s);
    }
}
