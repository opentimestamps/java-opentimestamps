package com.eternitywall.op;

import com.eternitywall.StreamDeserializationContext;

import java.util.logging.Logger;

/**
 * Cryptographic SHA256 operation
 * Cryptographic operation tag numbers taken from RFC4880, although it's not
 * guaranteed that they'll continue to match that RFC in the future.
 *
 * @extends CryptOp
 */
public class OpSHA256 extends OpCrypto {


    private static Logger log = Logger.getLogger(OpSHA256.class.getName());

    public static byte _TAG = 0x08;

    @Override
    public byte _TAG() {
        return OpSHA256._TAG;
    }

    @Override
    public String _TAG_NAME() {
        return "sha256";
    }

    @Override
    public String _HASHLIB_NAME() {
        return "SHA-256";
    }

    @Override
    public int _DIGEST_LENGTH() {
        return 32;
    }

    public OpSHA256() {
        super();
        this.arg = new byte[]{};
    }

    public OpSHA256(byte[] arg_) {
        super(new byte[]{});
        this.arg = arg_;
    }

    @Override
    public byte[] call(byte[] msg) {
        return super.call(msg);
    }

    public static Op deserializeFromTag(StreamDeserializationContext ctx, byte tag) {
        return OpCrypto.deserializeFromTag(ctx, tag);
    }

}