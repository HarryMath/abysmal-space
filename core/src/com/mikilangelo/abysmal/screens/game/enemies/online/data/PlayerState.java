package com.mikilangelo.abysmal.screens.game.enemies.online.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PlayerState extends DataPackage {
  private static final byte[] indicator = "state".getBytes(StandardCharsets.US_ASCII);
  public String generationId; // unique generation id
  public int shipId; // ship id
  public float x, y, angle;
  public float speedX, speedY, angularSpeed; // angular velocity
  public float health; // health
  public boolean isUnderControl; // is under control
  public float currentPower;
  public long timestamp; // timestamp

  public PlayerState() { }

  @Deprecated
  public void setNotCompressed(String string) {
    String[] data = string.substring(6, string.length() - 1).split(",");
    generationId = data[0];
    shipId = Integer.parseInt(data[1]);
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

  public PlayerState(byte[] data) {
    byte[][] chunks = split(data, (short) 12, (short) indicator.length);
    generationId = decodeString(chunks[0]);
    shipId = decodeInt(chunks[1]);
    x = decodeFloat(chunks[2]);
    y = decodeFloat(chunks[3]);
    angle = decodeFloat(chunks[4]);
    speedX = decodeFloat(chunks[5]);
    speedY = decodeFloat(chunks[6]);
    angularSpeed = decodeFloat(chunks[7]);
    health = decodeFloat(chunks[8]);
    isUnderControl = decodeBoolean(chunks[9]);
    currentPower = decodeFloat(chunks[10]);
    timestamp = decodeLong(chunks[11]);
  }

  public static boolean isInstance(byte[] data) {
    return startsWith(data, indicator);
  }

  public byte[] compress() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    outputStream.write(indicator);
    outputStream.write(generationId.getBytes(StandardCharsets.US_ASCII));
    outputStream.write(separator);
    outputStream.write(compress(shipId));
    outputStream.write(separator);
    outputStream.write(compress(x));
    outputStream.write(separator);
    outputStream.write(compress(y));
    outputStream.write(separator);
    outputStream.write(compress(angle));
    outputStream.write(separator);
    outputStream.write(compress(speedX));
    outputStream.write(separator);
    outputStream.write(compress(speedY));
    outputStream.write(separator);
    outputStream.write(compress(angularSpeed));
    outputStream.write(separator);
    outputStream.write(compress(health));
    outputStream.write(separator);
    outputStream.write(compress(isUnderControl));
    outputStream.write(separator);
    outputStream.write(compress(currentPower));
    outputStream.write(separator);
    outputStream.write(compress(timestamp));
    byte[] result = outputStream.toByteArray();
    outputStream.close();
    return result;
  }

  @Override
  @Deprecated
  public String toString() {
    return "state[" +
            generationId + "," +
            shipId + "," +
            x + "," +
            y + "," +
            angle + "," +
            speedX + "," +
            speedY + "," +
            angularSpeed + "," +
            health + "," +
            isUnderControl + "," +
            currentPower + "," +
            timestamp + "]";
  }

  /*
  old example 268 symbols:

  '{"generationId":"defender0202797818","shipId":"defender","x":-10.836355,"y":53.327827,
  "angle":0.81490326,"speedX":1.8270377,"speedY":6.214534,"angularSpeed":-0.0019457892,
  "health":44.9,"isUnderControl":true,"currentPower":0.0041643687,"timestamp":1650204282511}'

  csv:

  'state[defender0202797818,defender,-10.836355,53.327827,0.81490326,1.8270377,6.214534,-0.0019457892,
  45.0,false,4.1643687E-4,1650204282511]'

  final 54 symbols:

  '~@Eg,,?-a?,BUO?,?P??,???_,@??v,??	?,B4  ,f,9?U6,�?7?�?'

  */
}
