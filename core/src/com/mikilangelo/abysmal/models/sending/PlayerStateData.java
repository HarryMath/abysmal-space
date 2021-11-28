package com.mikilangelo.abysmal.models.sending;

public class PlayerStateData {
  public String g;
  public float x;
  public float y;
  public float aX; // acceleration X
  public float aY; // acceleration Y
  public float aA; // angular velocity
  public float a; // angle
  public float h; // health
  public boolean c; // is under control
  public long t; // timestamp

  public static boolean isInstance(String json) {
    return json.startsWith("{\"g");
  }

  @Override
  public String toString() {
    return "{\"g\":\"" + g + '\"' +
            ",\"x\":" + x +
            ",\"y\":" + y +
            ",\"aX\":" + aX +
            ",\"aY\":" + aY +
            ",\"aA\":" + aA +
            ",\"a\":" + a +
            ",\"h\":" + h +
            ",\"c\":" + c +
            ",\"t\":" + t + '}';
  }
}
