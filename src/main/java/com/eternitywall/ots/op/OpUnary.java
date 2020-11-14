package com.eternitywall.ots.op;

import com.eternitywall.ots.StreamDeserializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Operations that act on a single message.
 *
 * @see Op
 */
public abstract class OpUnary extends Op {

    private static Logger log = LoggerFactory.getLogger(OpUnary.class);

    @Override
    public String _TAG_NAME() {
        return "";
    }

    public OpUnary() {
        super();
    }

    public static Op deserializeFromTag(StreamDeserializationContext ctx, byte tag) {
        if (tag == OpSHA1._TAG) {
            return new OpSHA1();
        } else if (tag == OpSHA256._TAG) {
            return new OpSHA256();
        } else if (tag == OpRIPEMD160._TAG) {
            return new OpRIPEMD160();
        } else if (tag == OpKECCAK256._TAG) {
            return new OpKECCAK256();
        } else {
            log.warn("Unknown operation tag: {}", tag);

            return null;     // TODO: Is this OK? Won't it blow up later? Better to throw?
        }
    }

    @Override
    public String toString() {
        return this._TAG_NAME();
    }
}
