package com.mikilangelo.abysmal.screens.game.enemies.online.data;

import java.nio.charset.StandardCharsets;

public class BroadcastRequest extends DataPackage {
  private static final byte[] indicator = "req".getBytes(StandardCharsets.US_ASCII);

  public static boolean isInstance(byte[] data) {
    for (byte i = 0; i < indicator.length; i++) {
      if (indicator[i] != data[i]) {
        return false;
      }
    }
    return true;
  }

  @Override
  public byte[] compress() {
    return indicator;
  }
}
