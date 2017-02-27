package com.eternitywall;

/**
 * Prepend a prefix to a message.
 *
 * @extends com.eternitywall.OpBinary
 */
class OpPrepend extends OpBinary {

    byte[] arg;

    public static byte _TAG = (byte) 0xf1;

    @Override
    public String _TAG_NAME() {
        return "prepend";
    }

    OpPrepend() {
        super();
        this.arg = new byte[]{};
    }

    OpPrepend(byte[] arg_) {
        super(arg_);
        this.arg = arg_;
    }

    @Override
    public byte[] call(byte[] msg) {
        return Utils.arraysConcat(this.arg, msg);
    }

    public static Op deserializeFromTag(StreamDeserializationContext ctx, byte tag) {
        return OpBinary.deserializeFromTag(ctx, tag);
    }
}