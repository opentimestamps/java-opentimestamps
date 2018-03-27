package com.eternitywall.ots.op;

import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.Utils;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Prepend a prefix to a message.
 *
 * @see OpBinary
 */
public class OpReverse extends OpUnary {

    private static Logger log = Logger.getLogger(OpReverse.class.getName());

    byte[] arg;

    public static byte _TAG = (byte) 0xf2;

    @Override
    public byte _TAG() {
        return OpReverse._TAG;
    }

    @Override
    public String _TAG_NAME() {
        return "reverse";
    }

    @Override
    public byte[] call(byte[] msg) {
        return Utils.arrayReverse(msg);
    }

    public static Op deserializeFromTag(StreamDeserializationContext ctx, byte tag) {
        return OpBinary.deserializeFromTag(ctx, tag);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof OpReverse)){
            return false;
        }
        return Arrays.equals(this.arg,((OpReverse)obj).arg);
    }

    @Override
    public int hashCode(){
        return _TAG ^ Arrays.hashCode(this.arg);
    }
}