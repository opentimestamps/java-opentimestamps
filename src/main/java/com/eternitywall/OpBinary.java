package com.eternitywall;

import com.oracle.tools.packager.Log;

/**
 * Operations that act on a message and a single argument.
 *
 * @extends com.eternitywall.OpUnary
 */
class OpBinary extends Op {

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
        } else {
            Log.debug("Unknown operation tag: " + tag);
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
}