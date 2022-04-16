package com.mikilangelo.abysmal.screens.game.enemies.online.data;

public class PlayerDataRequest {
  public String playerId;

  public static boolean isInstance(String data) {
    return data.startsWith("{\"p");
  }

  @Override
  public String toString() {
    return "{\"playerId\":\"" + playerId + "\"}";
  }
}
