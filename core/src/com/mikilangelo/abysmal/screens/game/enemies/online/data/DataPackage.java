package com.mikilangelo.abysmal.screens.game.enemies.online.data;

import static java.nio.ByteOrder.BIG_ENDIAN;
import java.nio.ByteBuffer;

public abstract class DataPackage {

  protected static final byte separator = -12;

  public abstract byte[] compress();

  protected static String decodeString(byte[] bytes) {
    StringBuilder str = new StringBuilder();
    for (byte value : bytes) {
        str.append((char)value);
    }
    return str.toString();
  }

  protected static float decodeFloat(byte[] data) {
    return ByteBuffer.wrap(data).order(BIG_ENDIAN).getFloat();
  }

  protected static int decodeInt(byte[] data) {
    return ByteBuffer.wrap(data).order(BIG_ENDIAN).getInt();
  }

  protected static long decodeLong(byte[] data) {
    return ByteBuffer.wrap(data).order(BIG_ENDIAN).getLong();
  }

  protected static boolean decodeBoolean(byte[] data) {
    return data.length == 1 && data[0] == 1;
  }

  protected static byte[] get(byte[] data, int start, int len) {
    byte[] res = new byte[len];
    System.arraycopy(data, start, res, 0, len);
    return res;
  }

}
