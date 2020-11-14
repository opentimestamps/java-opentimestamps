package com.eternitywall.ots.op;

import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;
import com.eternitywall.ots.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Operations that act on a message and a single argument.
 *
 * @see OpUnary
 */
public abstract class OpBinary extends Op implements Comparable<Op> {

    private static final Logger log = LoggerFactory.getLogger(OpBinary.class);

    public byte[] arg;

    @Override
    public String _TAG_NAME() {
        return "";
    }

    public OpBinary() {
        super();
        this.arg = new byte[]{};
    }

    public OpBinary(byte[] arg_) {
        super();
        this.arg = arg_;
    }

    public static Op deserializeFromTag(StreamDeserializationContext ctx, byte tag) {
        byte[] arg = ctx.readVarbytes(_MAX_RESULT_LENGTH, 1);

        if (tag == OpAppend._TAG) {
            return new OpAppend(arg);
        } else if (tag == OpPrepend._TAG) {
            return new OpPrepend(arg);
        } else {
            log.warn("Unknown operation tag: {} 0x{}", tag, String.format("%02x", tag));
            return null;     // TODO: Is this OK? Won't it blow up later? Better to throw?
        }
    }

    @Override
    public void serialize(StreamSerializationContext ctx) {
        super.serialize(ctx);
        ctx.writeVarbytes(this.arg);
    }

    @Override
    public String toString() {
        return this._TAG_NAME() + ' ' + Utils.bytesToHex(this.arg).toLowerCase();
    }

    @Override
    public int compareTo(Op o) {
        if (o instanceof OpBinary && this._TAG() == o._TAG()) {
            return Utils.compare(this.arg, ((OpBinary) o).arg);
        }

        return this._TAG() - o._TAG();
    }

    @Override
    public int hashCode() {
        return _TAG ^ Arrays.hashCode(this.arg);
    }
}
