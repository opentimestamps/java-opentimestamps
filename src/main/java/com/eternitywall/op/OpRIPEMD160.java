package com.eternitywall.op;

import com.eternitywall.StreamDeserializationContext;
import com.eternitywall.crypto.RIPEMD160Digest;

import java.util.logging.Logger;

/**
 * Cryptographic RIPEMD160 operation
 * Cryptographic operation tag numbers taken from RFC4880, although it's not
 * guaranteed that they'll continue to match that RFC in the future.
 *
 * @see com.eternitywall.op.OpCrypto
 */
public class OpRIPEMD160 extends OpCrypto {

    private static Logger log = Logger.getLogger(OpRIPEMD160.class.getName());

    public static byte _TAG = 0x03;

    @Override
    public byte _TAG() {
        return OpRIPEMD160._TAG;
    }

    @Override
    public String _TAG_NAME() {
        return "ripemd160";
    }

    @Override
    public String _HASHLIB_NAME() {
        return "ripemd160";
    }

    @Override
    public int _DIGEST_LENGTH() {
        return 20;
    }

    OpRIPEMD160() {
        super();
        this.arg = new byte[]{};
    }

    OpRIPEMD160(byte[] arg_) {
        super(arg_);
        this.arg = arg_;
    }

    @Override
    public byte[] call(byte[] msg) {
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(msg, 0, msg.length);
        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        return hash;
    }

    public static Op deserializeFromTag(StreamDeserializationContext ctx, byte tag) {
        return OpCrypto.deserializeFromTag(ctx, tag);
    }

}