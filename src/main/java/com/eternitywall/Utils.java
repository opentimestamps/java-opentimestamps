package com.eternitywall;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by luca on 26/02/2017.
 */
public class Utils {


    public static byte[] arraysConcat(byte[] array1, byte[] array2) {
        byte[] array1and2 = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, array1and2, 0, array1.length);
        System.arraycopy(array2, 0, array1and2, array1.length, array2.length);
        return array1and2;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static byte[] randBytes(int length) throws NoSuchAlgorithmException {
        //Java 6 & 7:
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);

        //Java 8 (even more secure):
        /*byte[] bytes = new byte[length];
        SecureRandom.getInstanceStrong().nextBytes(bytes);*/

        return bytes;
    }

    public static byte[] arrayReverse(byte[] array) {
        byte[] reversedArray = new byte[array.length];
        for (int i = array.length - 1, j = 0; i >= 0; i--, j++) {
            reversedArray[j] = array[i];
        }
        return reversedArray;
    }

    public static byte[] hexToBytes(String hexString) {
        int len = hexString.length();
        if(len%2!=0)
            throw new IllegalArgumentException();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {

            int hi = Character.digit(hexString.charAt(i), 16);
            int lo = Character.digit(hexString.charAt(i + 1), 16);
            if ((hi < 0) || (lo < 0))
                throw new IllegalArgumentException();
            data[i / 2] = (byte) ((hi << 4)
                    + lo);
        }
        return data;
    }

    public static int compare(byte[] left, byte[] right) {
        for (int i = 0, j = 0; i < left.length && j < right.length; i++, j++) {
            int a = (left[i] & 0xff);
            int b = (right[j] & 0xff);
            if (a != b) {
                return a - b;
            }
        }
        return left.length - right.length;
    }
}
