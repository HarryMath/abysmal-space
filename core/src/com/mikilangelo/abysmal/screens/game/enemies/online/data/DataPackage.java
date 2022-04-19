package com.mikilangelo.abysmal.screens.game.enemies.online.data;

import static java.nio.ByteOrder.BIG_ENDIAN;
import java.nio.ByteBuffer;


public abstract class DataPackage {

  private static long success = 0;
  private static int failsComa = 0;
  private static int failsSpace = 0;

  private String compress(ByteBuffer bytes) {
    StringBuilder str = new StringBuilder();
    boolean wasNonEmptyByte = false;
    for (byte value : bytes.array()) {
      if (wasNonEmptyByte || value != 0) {
        wasNonEmptyByte = true;
        str.append((char)value);
      }
    }
    success ++;
    if (str.toString().contains(".")) {
      System.out.println("Error encoding. contains \"dot\"");
      failsComa ++;
      System.out.println("dot appears at " + failsComa + " of " + success + " compressions");
    }
    return str.toString();
  }

  protected String compress(int a) {
    return compress(ByteBuffer.allocate(4).putInt(a));
  }

  protected String compress(long a) {
    return compress(ByteBuffer.allocate(8).putLong(a));
  }

  protected String compress(float a) {
    return compress(ByteBuffer.allocate(4).putFloat(a));
  }

  protected String compress(boolean a) {
    return a ? "t" : "f";
  }

  protected float decodeFloat(String s) {
    final char[] chars = s.toCharArray();
    final byte[] bytes = new byte[4];
    int i = 4 - chars.length;
    for (char value : chars) {
      bytes[i++] = (byte) value;
    }
    return ByteBuffer.wrap(bytes).order(BIG_ENDIAN).getFloat();
  }

  protected int decodeInt(String s) {
    final char[] chars = s.toCharArray();
    final byte[] bytes = new byte[4];
    int i = 4 - chars.length;
    for (char value : chars) {
      bytes[i++] = (byte) value;
    }
    return ByteBuffer.wrap(bytes).order(BIG_ENDIAN).getInt();
  }

  protected long decodeLong(String s) {
    final char[] chars = s.toCharArray();
    final byte[] bytes = new byte[8];
    int i = 8 - chars.length;
    for (char value : chars) {
      bytes[i++] = (byte) value;
    }
    return ByteBuffer.wrap(bytes).order(BIG_ENDIAN).getLong();
  }

  protected boolean decodeBoolean(String s) {
    return "t".equals(s);
  }
}
