package com.eternitywall.ots;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VerifyResult implements Comparable<VerifyResult> {

    private static final String DATE_PATTERN = "YYYY-MM-dd z";
    private static final DateFormatSymbols DATE_FORMAT_SYMBOLS = new DateFormatSymbols(new Locale("en", "UK"));

    public enum Chains {
        BITCOIN, LITECOIN, ETHEREUM
    }

    public Long timestamp;
    int height;

    VerifyResult(Long timestamp, int height){
        this.timestamp = timestamp;
        this.height = height;
    }
    /**
     * Returns, if existing, a string representation describing the existence of a block attest
     */
    public String toString() {
        if (height == 0 || timestamp == null) {
            return "";
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN, DATE_FORMAT_SYMBOLS);
        String dateString = simpleDateFormat.format(new Date(timestamp * 1000));

        return "block " + height + " attests data existed as of " + dateString;
    }

    @Override
    public int compareTo(VerifyResult other) {
        return height - other.height;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof VerifyResult)) {
            return false;
        }

        VerifyResult vr = (VerifyResult) other;

        return timestamp.equals(vr.timestamp) && height == vr.height;
    }

    @Override
    public int hashCode(){
        return ((int) (long)(timestamp)) ^ height;
    }
}
