package com.mikilangelo.abysmal.screens.game.enemies.online.data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BroadcastRequest extends DataPackage {
  private static final byte[] indicator = "req".getBytes(StandardCharsets.US_ASCII);

  public static boolean isInstance(byte[] data) {
    return startsWith(data, indicator);
  }

  @Override
  public byte[] compress() {
    return indicator;
  }
}
