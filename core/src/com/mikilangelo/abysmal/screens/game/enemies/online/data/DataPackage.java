package com.mikilangelo.abysmal.screens.game.enemies.online.data;

import static java.nio.ByteOrder.BIG_ENDIAN;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public abstract class DataPackage {

  protected static final byte separator = -12;

  public abstract byte[] compress() throws IOException;

  private static byte[] compress(byte[] bytes) {
    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
    for (byte i = 0; i < bytes.length; i++) {
      if (bytes[i] != 0) {
        byte[] result = new byte[bytes.length - i];
        byteBuffer.get(result, 0, bytes.length - i);
        for (short j =0 ; j < result.length; j++) {
          if (result[j] == separator) {
            result[j] = separator + 1;
            System.out.println("byte is " + separator);
          }
        }
        return result;
      } else {
        byteBuffer.get();
      }
    }
    return new byte[]{};
  }

  protected static String decodeString(byte[] bytes) {
    StringBuilder str = new StringBuilder();
    for (byte value : bytes) {
        str.append((char)value);
    }
    return str.toString();
  }

  protected static byte[] compress(int a) {
    return compress(ByteBuffer.allocate(4).putInt(a).array());
  }

  protected static byte[] compress(long a) {
    return compress(ByteBuffer.allocate(8).putLong(a).array());
  }

  protected static byte[] compress(float a) {
    return compress(ByteBuffer.allocate(4).putFloat(a).array());
  }

  protected byte[] compress(boolean a) {
    return a ? new byte[]{1} : new byte[]{};
  }

  protected static float decodeFloat(byte[] data) {
    final byte[] bytes = new byte[4];
    int i = 4 - data.length;
    for (byte value : data) {
      bytes[i++] = value;
    }
    return ByteBuffer.wrap(bytes).order(BIG_ENDIAN).getFloat();
  }

  protected int decodeInt(byte[] data) {
    final byte[] bytes = new byte[4];
    int i = 4 - data.length;
    for (byte value : data) {
      bytes[i++] = value;
    }
    return ByteBuffer.wrap(bytes).order(BIG_ENDIAN).getInt();
  }

  protected static long decodeLong(byte[] data) {
    final byte[] bytes = new byte[8];
    int i = 8 - data.length;
    for (byte value : data) {
      bytes[i++] = value;
    }
    return ByteBuffer.wrap(bytes).order(BIG_ENDIAN).getLong();
  }

  protected boolean decodeBoolean(byte[] data) {
    return data.length == 1 && data[0] == 1;
  }

  protected static boolean startsWith(byte[] data, byte[] prefix) {
    for (byte i = 0; i < prefix.length; i++) {
      if (data[i] != prefix[i]) {
        return false;
      }
    }
    return true;
  }

  protected static byte[][] split(byte[] data, short amount, short offset) {
    // ByteBuffer byteBuffer = ByteBuffer.wrap(data);
    byte[][] result = new byte[amount][];
    short start = offset;
    byte packageNumber = 0;
    for (short i = offset; i < data.length; i++) {
      if (data[i] == separator) {
        result[packageNumber] = new byte[i - start];
        // byteBuffer.get(result[packageNumber], packageNumber, i - start - packageNumber);
        System.arraycopy(data, start, result[packageNumber], 0, i - start);
        packageNumber++;
        start = (short)(i + 1);
      }
    }
    if (packageNumber == amount - 1) {
      result[packageNumber] = new byte[data.length - start];
      // byteBuffer.get(result[packageNumber], 0, data.length - start);
      System.arraycopy(data, start, result[packageNumber], 0, data.length - start);
    }
    return result;
  }
}
