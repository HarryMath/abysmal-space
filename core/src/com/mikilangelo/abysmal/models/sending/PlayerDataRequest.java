package com.mikilangelo.abysmal.models.sending;

public class PlayerDataRequest {
  public String playerId;

  public static boolean isInstance(String json) {
    return json.startsWith("{\"p");
  }

  @Override
  public String toString() {
    return "{\"playerId\":\"" + playerId + "\"}";
  }
}
