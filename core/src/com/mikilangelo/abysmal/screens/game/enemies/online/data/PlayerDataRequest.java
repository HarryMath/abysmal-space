package com.mikilangelo.abysmal.screens.game.enemies.online.data;

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
