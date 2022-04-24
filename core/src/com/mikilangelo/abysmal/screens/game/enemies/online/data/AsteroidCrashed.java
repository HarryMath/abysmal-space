package com.mikilangelo.abysmal.screens.game.enemies.online.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AsteroidCrashed extends DataPackage {

  private static final byte[] indicator = "ast".getBytes(StandardCharsets.US_ASCII);
  public long asteroidId;
  public float x;
  public float y;
  public float angle;
  public long timestamp;

  public AsteroidCrashed() { }

  public AsteroidCrashed(byte[] data) {
    byte[][] chunks = split(data, (short) 5, (short) indicator.length);
    asteroidId = decodeLong(chunks[0]);
    x = decodeFloat(chunks[1]);
    y = decodeFloat(chunks[2]);
    angle = decodeFloat(chunks[3]);
    timestamp = decodeLong(chunks[4]);
  }

  public static boolean isInstance(byte[] data) {
    return startsWith(data, indicator);
  }

  @Override
  public String toString() {
    return "ast[" +
            asteroidId + "," +
            x + "," +
            y + "," +
            angle + "," +
            timestamp + "]";
  }

  @Override
  public byte[] compress() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    outputStream.write(indicator);
    outputStream.write(compress(asteroidId));
    outputStream.write(separator);
    outputStream.write(compress(x));
    outputStream.write(separator);
    outputStream.write(compress(y));
    outputStream.write(separator);
    outputStream.write(compress(angle));
    outputStream.write(separator);
    outputStream.write(compress(timestamp));
    byte[] result = outputStream.toByteArray();
    outputStream.close();
    return result;
  }
}
