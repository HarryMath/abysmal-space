package com.mikilangelo.abysmal.models.sending;

public class ShotData {
  public float x, y, angle;
  public float impulseX, impulseY;
  public boolean hasTurret;
  public long timestamp;
  public String generationId;

  public ShotData() {}

  public ShotData(String string) {
    String[] data = string.substring(5, string.length() - 1).split(",");
    x = Float.parseFloat(data[0]);
    y = Float.parseFloat(data[1]);
    angle = Float.parseFloat(data[2]);
    impulseX = Float.parseFloat(data[3]);
    impulseY = Float.parseFloat(data[4]);
    hasTurret = Boolean.parseBoolean(data[5]);
    timestamp = Long.parseLong(data[6]);
    generationId = data[7];

  }

  public static boolean isInstance(String json) {
    return json.startsWith("shot[");
  }

  @Override
  public String toString() {
    return "shot[" + x + ',' + y + ',' + angle + ',' +
            impulseX +',' + impulseY + ',' + hasTurret + ',' +
            timestamp + ',' + generationId + ']';
  }
}
