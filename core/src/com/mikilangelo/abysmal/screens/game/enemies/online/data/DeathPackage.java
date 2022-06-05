package com.mikilangelo.abysmal.screens.game.enemies.online.data;

import java.nio.charset.StandardCharsets;

public class DeathPackage extends DataPackage {

  private static final short length = 4; // 16

  public String generationId;

  public DeathPackage(String generationId) {
    this.generationId = generationId;
  }

  public DeathPackage(byte[] data) {
    generationId = decodeString(data);
  }

  public static boolean isInstance(byte[] data) {
    return data.length == length;
  }

  public byte[] compress() {
    return generationId.getBytes(StandardCharsets.UTF_8);
  }
}
