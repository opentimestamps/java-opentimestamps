package com.eternitywall.ots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class StreamDeserializationContext {

    private static final Logger log = LoggerFactory.getLogger(StreamDeserializationContext.class);

    byte[] buffer;
    int counter = 0;

    public StreamDeserializationContext(byte[] stream) {
        this.buffer = stream;
        this.counter = 0;
    }

    public byte[] getOutput() {
        return this.buffer;
    }

    public int getCounter() {
        return this.counter;
    }

    public byte[] read(int l) {
        if (this.counter == this.buffer.length) {
            return null;
        }

        if (l > this.buffer.length) {
            l = this.buffer.length;
        }

        // const uint8Array = new Uint8Array(this.buffer,this.counter,l);
        byte[] uint8Array = Arrays.copyOfRange(this.buffer, this.counter, this.counter + l);
        this.counter += l;

        return uint8Array;
    }

    public boolean readBool() {
        byte b = this.read(1)[0];

        if (b == 0xff) {
            return true;
        } else if (b == 0x00) {
            return false;
        }

        return false;
    }

    public int readVaruint() {
        int value = 0;
        byte shift = 0;
        byte b;

        do {
            b = this.read(1)[0];
            value |= (b & 0b01111111) << shift;
            shift += 7;
        } while ((b & 0b10000000) != 0b00000000);

        return value;
    }

    public byte[] readBytes(int expectedLength) {
        if (expectedLength == 0) {
            return this.readVarbytes(1024, 0);
        }

        return this.read(expectedLength);
    }

    public byte[] readVarbytes(int maxLen) {
        return readVarbytes(maxLen, 0);
    }

    public byte[] readVarbytes(int maxLen, int minLen) {
        int l = this.readVaruint();

        if ((l & 0xff) > maxLen) {
            log.warn("varbytes max length exceeded;");
            return null;
        } else if ((l & 0xff) < minLen) {
            log.warn("varbytes min length not met;");
            return null;
        }

        return this.read(l);
    }

    public boolean assertMagic(byte[] expectedMagic) {
        byte[] actualMagic = this.read(expectedMagic.length);

        return Arrays.equals(expectedMagic, actualMagic);
    }

    public boolean assertEof() {
        byte[] excess = this.read(1);
        return excess != null;
    }

    public String toString() {
        return Arrays.toString(this.buffer);
    }
}
