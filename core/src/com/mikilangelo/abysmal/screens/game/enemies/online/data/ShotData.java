package com.mikilangelo.abysmal.screens.game.enemies.online.data;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ShotData extends DataPackage {

  private static final int length = 4 * 6 + 1 + 8 + 4; // 37

  public float x, y, angle;
  public float impulseX, impulseY;
  public int gunId;
  public boolean withSound;
  public long timestamp;
  public String generationId;

  public ShotData() {
  }

  public ShotData(float x, float y, float angle, float impulseX, float impulseY, int gunId, boolean withSound, long timestamp, String generationId) {
    this.x = x;
    this.y = y;
    this.angle = angle;
    this.impulseX = impulseX;
    this.impulseY = impulseY;
    this.gunId = gunId;
    this.withSound = withSound;
    this.timestamp = timestamp;
    this.generationId = generationId;
  }

  public ShotData(byte[] data) {
    x = decodeFloat(get(data, 0, 4));
    y = decodeFloat(get(data, 4, 4));
    angle = decodeFloat(get(data, 8, 4));
    impulseX = decodeFloat(get(data, 12, 4));
    impulseY = decodeFloat(get(data, 16, 4));
    gunId = decodeInt(get(data, 20, 4));
    withSound = decodeBoolean(get(data, 24, 1));
    timestamp = decodeLong(get(data, 25, 8));
    generationId = decodeString(get(data, 33, 4));
  }

  public static boolean isInstance(byte[] data) {
    return data.length == length;
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
  public byte[] compress() {
    return ByteBuffer.allocate(length)
            .putFloat(x)
            .putFloat(y)
            .putFloat(angle)
            .putFloat(impulseX)
            .putFloat(impulseY)
            .putFloat(gunId)
            .put(withSound ? (byte) 1 : (byte) 0)
            .putLong(timestamp)
            .put(generationId.getBytes(StandardCharsets.US_ASCII))
            .array();
  }
}
