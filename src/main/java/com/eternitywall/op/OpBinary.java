package com.eternitywall.op;

import com.eternitywall.StreamDeserializationContext;
import com.eternitywall.StreamSerializationContext;
import com.eternitywall.Utils;

import java.util.logging.Logger;

/**
 * Operations that act on a message and a single argument.
 *
 * @extends com.eternitywall.op.OpUnary
 */
public class OpBinary extends Op implements Comparable<Op> {

    private static Logger log = Logger.getLogger(OpBinary.class.getName());

    byte[] arg;

    @Override
    public String _TAG_NAME() {
        return "";
    }

    OpBinary() {
        super();
        this.arg = new byte[]{};
    }

    OpBinary(byte[] arg_) {
        super();
        this.arg = arg_;
    }

    public static Op deserializeFromTag(StreamDeserializationContext ctx, byte tag) {
        byte[] arg = ctx.readVarbytes(_MAX_RESULT_LENGTH, 1);
        if (tag == OpAppend._TAG) {
            return new OpAppend(arg);
        } else if (tag == OpPrepend._TAG) {
            return new OpPrepend(arg);
        } else if (tag == OpSHA1._TAG) {
            return new OpSHA1(arg);
        } else if (tag == OpSHA256._TAG) {
            return new OpSHA256(arg);
        } else if (tag == OpRIPEMD160._TAG) {
            return new OpRIPEMD160(arg);
        } else if (tag == OpKECCAK256._TAG) {
            return new OpRIPEMD160(arg);
        } else {
            log.severe("Unknown operation tag: " + tag  + " 0x" + String.format("%02x", tag));
            return null;
        }
    }

    @Override
    public void serialize(StreamSerializationContext ctx) {
        super.serialize(ctx);
        ctx.writeVarbytes(this.arg);
    }

    @Override
    public String toString() {
        return this._TAG_NAME() + ' ' + Utils.bytesToHex(this.arg);
    }


    @Override
    public int compareTo(Op o) {
        if(o instanceof OpBinary && this._TAG()==o._TAG()) {
            return Utils.compare(this.arg, ((OpBinary) o).arg );
        }
        return this._TAG()-o._TAG();

    }

}