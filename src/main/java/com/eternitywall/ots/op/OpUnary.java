package com.eternitywall.ots.op;

import com.eternitywall.ots.StreamDeserializationContext;

import java.util.logging.Logger;

/**
 * Operations that act on a single message.
 *
 * @see com.eternitywall.ots.op.Op
 */
public class OpUnary extends Op {


    private static Logger log = Logger.getLogger(OpUnary.class.getName());

    byte[] arg;

    @Override
    public String _TAG_NAME() {
        return "";
    }

    OpUnary() {
        super();
        this.arg = new byte[]{};
    }

    OpUnary(byte[] arg_) {
        super();
        this.arg = arg_;
    }

    public static Op deserializeFromTag(StreamDeserializationContext ctx, byte tag) {
        if (tag == OpAppend._TAG) {
            return new OpAppend();
        } else if (tag == OpPrepend._TAG) {
            return new OpPrepend();
        } else if (tag == OpSHA1._TAG) {
            return new OpSHA1();
        } else if (tag == OpSHA256._TAG) {
            return new OpSHA256();
        } else if (tag == OpRIPEMD160._TAG) {
            return new OpRIPEMD160();
        } else if (tag == OpKECCAK256._TAG) {
            return new OpKECCAK256();
        } else {
            log.severe("Unknown operation tag: " + tag);
            return null;
        }
    }


    @Override
    public String toString() {
        return this._TAG_NAME();
    }
}