package com.eternitywall;

/**
 * Cryptographic SHA256 operation
 * Cryptographic operation tag numbers taken from RFC4880, although it's not
 * guaranteed that they'll continue to match that RFC in the future.
 * @extends CryptOp
 */
class OpSHA256 extends OpCrypto {

    public static byte _TAG = 0x08;

    @Override
    public String _TAG_NAME() {
        return "sha256";
    }

    @Override
    public String _HASHLIB_NAME() {
        return "SHA-256";
    }

    @Override
    public int _DIGEST_LENGTH(){ return 32;}

    OpSHA256() {
        super();
        this.arg = new byte[]{};
    }
    OpSHA256(byte[] arg_) {
        super(new byte[]{});
        this.arg = arg_;
    }

    @Override
    public byte[] call(byte[] msg) {
        return super.call(msg);
    }

    public static Op deserializeFromTag(StreamDeserializationContext ctx, byte tag) {
        return OpCrypto.deserializeFromTag(ctx,tag);
    }

}