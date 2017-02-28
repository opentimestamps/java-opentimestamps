package com.eternitywall;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static com.eternitywall.Utils.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by casatta on 27/02/17.
 */
public class TestUtils {

    @Test
    public void testArraysConcat() {
        byte[] array = "foo".getBytes();
        byte[] array2 = "bar".getBytes();
        byte[] array3 = arraysConcat(array,array2);
        String str = new String(array3, StandardCharsets.UTF_8);
        assertTrue("foobar".equals(str));
    }

    @Test
    public void testBytesToHex() {
        byte[] array = " 0aZ".getBytes();
        String anObject = bytesToHex(array);
        assertTrue("2030615a".equals(anObject));
    }


    @Test
    public void testRandBytes() {
        try {
            byte[] array1 = randBytes(8);
            byte[] array2 = randBytes(8);
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
        byte[] array2 = arrayReverse(array1);
        byte[] array3 = arrayReverse(array2);
        assertFalse(Arrays.equals(array1,array2));
        assertTrue(Arrays.equals(array1,array3));
    }

    @Test
    public void testHexBytes() {
        try {
            hexToBytes("0");
            assertFalse(true);
        } catch (IllegalArgumentException e) {

        }

        try {
            hexToBytes("xx");
            assertFalse(true);
        } catch (IllegalArgumentException e) {

        }

        byte[] arr=hexToBytes("0000");
        assertTrue("0000".equals(bytesToHex(arr)));


        byte[] arr2=hexToBytes("0aef");
        assertTrue("0aef".equals(bytesToHex(arr2)));
    }


}

