package com.istt;

import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;

public class HexTest {

  @Test
  public void testHex() {
    byte[] data = {
      (byte) 0x12,
      (byte) 0x03,
      (byte) 0x01,
      (byte) 0x10,
      (byte) 0x20,
      (byte) 0x01,
      (byte) 0x0a,
      (byte) 0x00,
      (byte) 0x02,
      (byte) 0x0a,
      (byte) 0x08,
      (byte) 0x84,
      (byte) 0x10,
      (byte) 0x48,
      (byte) 0x49,
      (byte) 0x73,
      (byte) 0x26,
      (byte) 0x01,
      (byte) 0x09,
      (byte) 0x0a,
      (byte) 0x08,
      (byte) 0x84,
      (byte) 0x13,
      (byte) 0x48,
      (byte) 0x09,
      (byte) 0x99,
      (byte) 0x73,
      (byte) 0x82,
      (byte) 0x06,
      (byte) 0x00
    };
    System.out.println(Hex.encodeHexString(data));
  }

  @Test
  public void cicKeyTest() {
    String cicKey = "4567_8765_2";
    System.out.println(Integer.parseInt(cicKey.split("_")[2]));
  }
  
  @Test
  public void calledTest() {
    String cicKey = "84936414498F";
    System.out.println(cicKey.replaceAll("[^\\d.]", ""));
  }
}
