package com.mikilangelo.abysmal.models.sending;

public class ShotData {
  public float x, y, a;
  public float iX, iY;
  public boolean tur;
  public long t;
  public String g;

  public static boolean isInstance(String json) {
    return json.startsWith("{\"x");
  }

  @Override
  public String toString() {
    return "{\"x\":" + x +
            ",\"y\":" + y +
            ",\"a\":" + a +
            ",\"iX\":" + iX +
            ",\"iY\":" + iY +
            ",\"tur\":" + tur +
            ",\"t\":" + t +
            ",\"g\":\"" + g + "\"}";
  }
}
