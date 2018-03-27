package com.eternitywall.ots;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by luca on 25/02/2017.
 */
public class StreamSerializationContext {


    private static Logger log = Logger.getLogger(StreamSerializationContext.class.getName());

    List<Byte> buffer = new ArrayList<Byte>();

    public StreamSerializationContext() {
        this.buffer = new ArrayList<>();
    }

    public byte[] getOutput() {
        Byte[] bytesArray = (Byte[])this.buffer.toArray(new Byte[this.buffer.size()]);
        return toPrimitives(bytesArray);
    }
    public int getLength() {
        return this.buffer.size();
    }

    private byte[] toPrimitives(Byte[] oBytes)
    {
        byte[] bytes = new byte[oBytes.length];
        for(int i = 0; i < oBytes.length; i++){
            bytes[i] = oBytes[i];
        }
        return bytes;
    }


    public void writeBool(boolean value) {
        if (value == true) {
            this.writeByte((byte) 0xff);
        } else {
            this.writeByte((byte) 0x00);
        }
    }

    public void writeVaruint(int value) {
        if ((value) == 0b00000000) {
            this.writeByte((byte) 0x00);
        } else {
            while (value != 0) {
                byte b = (byte) ((value&0xff) & 0b01111111);
                if ((value) > 0b01111111) {
                    b |= 0b10000000;
                }
                this.writeByte(b);
                if ((value) <= 0b01111111) {
                    break;
                }
                value = value >> 7;
            }
        }
    }

    public void writeByte(byte value) {
        this.buffer.add( new Byte(value) );
    }

    public void writeByte(Byte value) {
        this.buffer.add( value );
    }


    public void writeBytes(byte[] value) {
        for (int i = 0; i < value.length; i++) {
            this.writeByte(value[i]);
        }
    }

    public void writeVarbytes(byte[] value) {
        this.writeVaruint(value.length);
        this.writeBytes(value);
    }

    public String toString() {
        return Arrays.toString( this.getOutput() );
    }

}
