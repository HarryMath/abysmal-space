package com.mikilangelo.abysmal.models.sending;

public class PlayerInitializingData {
  public String shipId;
  public float x;
  public float y;
  public String generationId;

  public static boolean isInstance(String json) {
    return json.startsWith("{\"s");
  }

  @Override
  public String toString() {
    return "{\"shipId\":\"" + shipId + '\"' +
            ",\"x\":" + x + ",\"y\":" + y +
            ",\"generationId\":\"" + generationId + "\"}";
  }
}
