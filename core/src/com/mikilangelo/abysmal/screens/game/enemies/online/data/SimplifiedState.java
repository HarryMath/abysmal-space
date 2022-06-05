package com.mikilangelo.abysmal.screens.game.enemies.online.data;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SimplifiedState extends DataPackage {
  private static final short length = 4 * 4; // 16

  public String generationId; // unique generation id
  public int shipId; // ship id
  public float x, y;

  public SimplifiedState(byte[] data) {
    generationId = decodeString(get(data, 0, 4));
    shipId = decodeInt(get(data, 4, 4));
    x = decodeFloat(get(data, 8, 4));
    y = decodeFloat(get(data, 12, 4));
  }

  public static boolean isInstance(byte[] data) {
    return data.length == length;
  }

  public byte[] compress() {
    return ByteBuffer.allocate(length)
            .put(generationId.getBytes(StandardCharsets.UTF_8))
            .putInt(shipId)
            .putFloat(x)
            .putFloat(y)
            .array();
  }

}
