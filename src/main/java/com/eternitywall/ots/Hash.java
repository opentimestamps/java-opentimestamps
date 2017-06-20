package com.eternitywall.ots;

import com.eternitywall.ots.op.*;
/**
 * Created by casatta on 03/03/17.
 */
public class Hash {
    private byte[] value;
    private byte algorithm;

    public Hash(byte[] value, byte algorithm) {
        this.value = value;
        this.algorithm = algorithm;
    }

    public Hash(byte[] value, String label) {
        this.value = value;
        this.algorithm = getOp(label)._TAG();
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public byte getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(byte algorithm) {
        this.algorithm = algorithm;
    }

    public OpCrypto getOp(){
        if (this.algorithm == OpSHA1._TAG){
            return new OpSHA1();
        } else if (this.algorithm == OpSHA256._TAG){
            return new OpSHA256();
        } else if (this.algorithm == OpRIPEMD160._TAG){
            return new OpRIPEMD160();
        } else if (this.algorithm == OpKECCAK256._TAG){
            return new OpKECCAK256();
        }
        return new OpSHA256();
    }
    public OpCrypto getOp(String label){
        if (label.toLowerCase().equals(new OpSHA1()._TAG_NAME())){
            return new OpSHA1();
        } else if (label.toLowerCase().equals(new OpSHA256()._TAG_NAME())){
            return new OpSHA256();
        } else if (label.toLowerCase().equals(new OpRIPEMD160()._TAG_NAME())){
            return new OpRIPEMD160();
        } else if (label.toLowerCase().equals(new OpKECCAK256()._TAG_NAME())){
            return new OpKECCAK256();
        }
        return new OpSHA256();
    }

}
