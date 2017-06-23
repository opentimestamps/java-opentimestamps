package com.eternitywall.ots.attestation;

import static org.junit.Assert.assertTrue;

import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import javax.xml.bind.DatatypeConverter;
import org.bitcoinj.core.Utils;
import org.junit.Test;


public class TestBitcoinBlockHeaderAttestation {

  @Test
  public void deserializationTrailingGarbage() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(DatatypeConverter.parseHexBinary("0588960d73d71901"+
        "02"+ // two bytes of payload
        "00" + // genesis block!
        "ff")); // one byte of trailing garbage

    StreamDeserializationContext ctx = new StreamDeserializationContext(baos.toByteArray());
    TimeAttestation.deserialize(ctx);
    // TODO exception TrailingGarbageError
  }

}