package com.mikilangelo.abysmal.screens.game.enemies.online.data;

import com.badlogic.gdx.math.MathUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BroadcastResponse extends DataPackage {

  private static final byte[] indicator = "res".getBytes(StandardCharsets.US_ASCII);
  public final long seed;
  public final long timestamp;

  public BroadcastResponse(long seed) {
    this.seed = seed;
    this.timestamp = System.currentTimeMillis();
  }

  public BroadcastResponse(byte[] data, int length) {
    String stringData = new String(data, indicator.length, length - indicator.length);
    seed = Long.parseLong(stringData.split(",")[0]);
    timestamp = Long.parseLong(stringData.split(",")[1]);
  }

  @Override
  public byte[] compress() {
    return (new String(indicator) + seed + ',' + timestamp).getBytes(StandardCharsets.US_ASCII);
  }

  public static void main(String[] a) {
    long seed = MathUtils.random(1923923929L);
    BroadcastResponse test = new BroadcastResponse(seed);
    System.out.println("original: {t: " + test.timestamp + ", seed: " + seed + "}");
    String encoded = new String(test.compress());
    System.out.println("encoded:  " + encoded);
    BroadcastResponse decoded = new BroadcastResponse(test.compress(), 12);
    System.out.println("decoded:  {t: " + decoded.timestamp + ", seed: " + decoded.seed + "}");
  }
}
