package com.eternitywall.ots;

import static org.junit.Assert.*;

import java.io.*;
import java.util.logging.*;

import org.junit.Test;

public class TestOtsCli {

  @Test
  public void testCommandLineHandlesUpgradeCommandWithWrongFileName() throws Exception {
    StringLoggerForTest loggerForTest = new StringLoggerForTest();
    
    OtsCli.main(new String[]{"upgrade", "some_non_existent_file.name"});
    
    final String logContents = loggerForTest.contents();
    assertTrue("Upgrade with non existent file should log 'No valid file' error", 
               logContents.contains("No valid file"));
  }
  
  private class StringLoggerForTest {
    
    private ByteArrayOutputStream baos;
    private StreamHandler sh;

    public StringLoggerForTest() {
      Logger logger = Utils.getLogger(OtsCli.class.getName());
      this.baos = new ByteArrayOutputStream();
      this.sh = new StreamHandler(baos, new SimpleFormatter());
      this.sh.setLevel(Level.ALL);
      logger.addHandler(this.sh);
    }
    
    public String contents() throws Exception {
      this.sh.flush();
      this.baos.close();
      return this.baos.toString();
    }
  }

}
