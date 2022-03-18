package com.mikilangelo.abysmal.screens.game.enemies.online.data;

public class PlayerState {
  public String generationId; // unique generation id
  public String shipName; // ship id
  public float x, y, angle;
  public float speedX, speedY, angularSpeed; // angular velocity
  public float health; // health
  public boolean isUnderControl; // is under control
  public float currentPower;
  public long timestamp; // timestamp

  public PlayerState() { }

  public PlayerState(String string) {
    String[] data = string.substring(6, string.length() - 1).split(",");
    generationId = data[0];
    shipName = data[1];
    x = Float.parseFloat(data[2]);
    y = Float.parseFloat(data[3]);
    angle = Float.parseFloat(data[4]);
    speedX = Float.parseFloat(data[5]);
    speedY = Float.parseFloat(data[6]);
    angularSpeed = Float.parseFloat(data[7]);
    health = Float.parseFloat(data[8]);
    isUnderControl = Boolean.parseBoolean(data[9]);
    currentPower = Float.parseFloat(data[10]);
    timestamp = Long.parseLong(data[11]);
  }

  public static boolean isInstance(String json) {
    return json.startsWith("state[");
  }

  @Override
  public String toString() {
    return "state[" + generationId + ',' + shipName + ',' +
            x + ',' + y + ',' + angle + ',' +
            speedX + ',' + speedY + ',' + angularSpeed + ',' +
            health + ',' + isUnderControl + ',' + currentPower + ',' + timestamp + ']';
  }
}
