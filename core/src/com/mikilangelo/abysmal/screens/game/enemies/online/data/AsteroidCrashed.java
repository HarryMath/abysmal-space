package com.mikilangelo.abysmal.screens.game.enemies.online.data;

import java.nio.ByteBuffer;

public class AsteroidCrashed extends DataPackage {

  private static final int length = 8 * 2 + 4 * 3; // 28

  public long asteroidId;
  public float x;
  public float y;
  public float angle;
  public long timestamp;

  public AsteroidCrashed() { }

  public AsteroidCrashed(byte[] data) {
    asteroidId = decodeLong(get(data, 0, 8));
    x = decodeFloat(get(data, 8, 4));
    y = decodeFloat(get(data, 12, 4));
    angle = decodeFloat(get(data, 16, 4));
    timestamp = decodeLong(get(data, 20, 8));
  }

  public static boolean isInstance(byte[] data) {
    return data.length == length;
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
  public byte[] compress() {
    return ByteBuffer.allocate(length)
            .putLong(asteroidId)
            .putFloat(x)
            .putFloat(y)
            .putFloat(angle)
            .putLong(timestamp)
            .array();
  }
}
