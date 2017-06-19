package com.eternitywall;

import com.eternitywall.ots.Utils;
import org.junit.Test;


import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by casatta on 27/02/17.
 */
public class TestUtils {

    Utils utils = new Utils();

    @Test
    public void arraysCopy() throws Exception {
        byte[] arr = Utils.hexToBytes("001100");
        byte[] arr2 = Utils.arraysCopy(arr);
        assertTrue( Utils.compare(arr,arr2)==0 );
        assertFalse(arr==arr2);
        arr[0]=(byte)13;
        assertTrue( Utils.compare(arr,arr2)>0 );
        assertNull(Utils.arraysCopy(null));
    }


    @Test
    public void arraysConcat() {
        byte[] array = "foo".getBytes();
        byte[] array2 = "bar".getBytes();
        byte[] array3 = Utils.arraysConcat(array,array2);
        String str = new String(array3, StandardCharsets.UTF_8);
        assertTrue("foobar".equals(str));
    }

    @Test
    public void bytesToHex() {
        byte[] array = " 0aZ".getBytes();
        String anObject = Utils.bytesToHex(array).toLowerCase();
        assertTrue("2030615a".equals(anObject));
    }


    @Test
    public void testRandBytes() {
        try {
            byte[] array1 = Utils.randBytes(8);
            byte[] array2 = Utils.randBytes(8);
            assertTrue(array1.length==8);
            assertTrue(array2.length==8);
            assertFalse(Arrays.equals(array1,array2));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testArrayReverse() {
        byte[] array1 = "0125121512512".getBytes();
        byte[] array2 = Utils.arrayReverse(array1);
        byte[] array3 = Utils.arrayReverse(array2);
        assertFalse(Arrays.equals(array1,array2));
        assertTrue(Arrays.equals(array1,array3));
    }

    @Test
    public void testHexBytes() {
        try {
            Utils.hexToBytes("0");
            assertFalse(true);
        } catch (IllegalArgumentException e) {

        }

        try {
            Utils.hexToBytes("xx");
            assertFalse(true);
        } catch (IllegalArgumentException e) {

        }

        byte[] arr=Utils.hexToBytes("0000");
        assertTrue("0000".equals(Utils.bytesToHex(arr).toLowerCase()));


        byte[] arr2= Utils.hexToBytes("0aef");
        assertTrue("0aef".equals(Utils.bytesToHex(arr2).toLowerCase()));
    }

    @Test
    public void testArrayFill() {
        byte[] a = new byte[10];
        Utils.arrayFill(a, (byte)0);
        assertEquals("00000000000000000000", Utils.bytesToHex(a).toLowerCase());

    }
}

