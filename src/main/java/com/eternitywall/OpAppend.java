package com.eternitywall;

import java.util.logging.Logger;

/**
 * Append a suffix to a message.
 *
 * @extends com.eternitywall.OpBinary
 */
class OpAppend extends OpBinary {

    private static Logger log = Logger.getLogger(OpAppend.class.getName());

    byte[] arg;

    public static byte _TAG = (byte) 0xf0;

    @Override
    public String _TAG_NAME() {
        return "append";
    }

    OpAppend() {
        super();
        this.arg = new byte[]{};
    }

    OpAppend(byte[] arg_) {
        super(arg_);
        this.arg = arg_;
    }

    @Override
    public byte[] call(byte[] msg) {
        return Utils.arraysConcat(msg, this.arg);
    }

    public static Op deserializeFromTag(StreamDeserializationContext ctx, byte tag) {
        return OpBinary.deserializeFromTag(ctx, tag);
    }
}