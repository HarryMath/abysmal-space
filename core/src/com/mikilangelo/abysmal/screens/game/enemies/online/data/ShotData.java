package com.mikilangelo.abysmal.screens.game.enemies.online.data;

public class ShotData extends DataPackage {
  public float x, y, angle;
  public float impulseX, impulseY;
  public int gunId;
  public long timestamp;
  public String generationId;
  public boolean withSound;

  public ShotData() {
  }

  public ShotData(float x, float y, float angle, float impulseX, float impulseY, int gunId, long timestamp, String generationId) {
    this.x = x;
    this.y = y;
    this.angle = angle;
    this.impulseX = impulseX;
    this.impulseY = impulseY;
    this.gunId = gunId;
    this.timestamp = timestamp;
    this.generationId = generationId;
  }

  public ShotData(String string) {
    String[] data = string.substring(5, string.length() - 1).split(",");
    x = Float.parseFloat(data[0]);
    y = Float.parseFloat(data[1]);
    angle = Float.parseFloat(data[2]);
    impulseX = Float.parseFloat(data[3]);
    impulseY = Float.parseFloat(data[4]);
    gunId = Integer.parseInt(data[5]);
    withSound = Boolean.parseBoolean(data[6]);
    timestamp = Long.parseLong(data[7]);
    generationId = data[8];

  }

  public static boolean isInstance(String data) {
    return data.startsWith("shot[");
  }

  @Override
  public String toString() {
    return "shot[" + x + ',' + y + ',' + angle + ',' +
            impulseX + ',' + impulseY + ',' + gunId + ',' +
            withSound + ',' + timestamp + ',' + generationId + ']';
  }
}
