package com.mikilangelo.abysmal.screens.game.enemies.online.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ShotData extends DataPackage {

  private static final byte[] indicator = "shot".getBytes(StandardCharsets.US_ASCII);
  public float x, y, angle;
  public float impulseX, impulseY;
  public int gunId;
  public long timestamp;
  public String generationId;
  public boolean withSound;

  public ShotData() {
  }

  public ShotData(float x, float y, float angle, float impulseX, float impulseY, int gunId, long timestamp, String generationId) {
    this.x = x;
    this.y = y;
    this.angle = angle;
    this.impulseX = impulseX;
    this.impulseY = impulseY;
    this.gunId = gunId;
    this.timestamp = timestamp;
    this.generationId = generationId;
  }

  public ShotData(byte[] data) {
    byte[][] chunks = split(data, (short) 9, (short) indicator.length);
    x = decodeFloat(chunks[0]);
    y = decodeFloat(chunks[1]);
    angle = decodeFloat(chunks[2]);
    impulseX = decodeFloat(chunks[3]);
    impulseY = decodeFloat(chunks[4]);
    gunId = decodeInt(chunks[5]);
    withSound = decodeBoolean(chunks[6]);
    timestamp = decodeLong(chunks[7]);
    generationId = decodeString(chunks[8]);
  }

  public static boolean isInstance(byte[] data) {
    return startsWith(data, indicator);
  }

  @Override
  public String toString() {
    return "shot[" +
            x + "," +
            y + "," +
            angle + "," +
            impulseX + "," +
            impulseY + "," +
            gunId + "," +
            withSound + "," +
            timestamp + ", " +
            generationId + ']';
  }

  @Override
  public byte[] compress() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    outputStream.write(indicator);
    outputStream.write(compress(x));
    outputStream.write(separator);
    outputStream.write(compress(y));
    outputStream.write(separator);
    outputStream.write(compress(angle));
    outputStream.write(separator);
    outputStream.write(compress(impulseX));
    outputStream.write(separator);
    outputStream.write(compress(impulseY));
    outputStream.write(separator);
    outputStream.write(compress(gunId));
    outputStream.write(separator);
    outputStream.write(compress(withSound));
    outputStream.write(separator);
    outputStream.write(compress(timestamp));
    outputStream.write(separator);
    outputStream.write(generationId.getBytes(StandardCharsets.US_ASCII));
    byte[] result = outputStream.toByteArray();
    outputStream.close();
    return result;
  }
}
