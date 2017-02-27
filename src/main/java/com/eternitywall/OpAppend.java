package com.eternitywall;

/**
 * Append a suffix to a message.
 * @extends com.eternitywall.OpBinary
 */
class OpAppend extends OpBinary {

    byte[] arg;

    public static byte _TAG= (byte)0xf0;

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
        return Utils.ArraysConcat(msg,this.arg);
    }

    public static Op deserializeFromTag(StreamDeserializationContext ctx, byte tag) {
        return OpBinary.deserializeFromTag(ctx,tag);
    }
}