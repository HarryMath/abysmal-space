package com.mikilangelo.abysmal.screens.game.enemies.online.data;

import com.google.gson.Gson;
import com.mikilangelo.abysmal.shared.tools.CalculateUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PlayerState extends DataPackage {
  private static final short length = 4 + 4 + 4 * 3 + 4 * 3 + 4 + 1 + 1 + 4 + 8; // 50

  public String generationId; // unique generation id
  public int shipId; // ship id
  public float x, y, angle;
  public float speedX, speedY, angularSpeed; // angular velocity
  public float health; // health
  public boolean isUnderControl; // is under control
  public boolean shieldOn;
  public float currentPower;
  public long timestamp; // timestamp

  private static final Gson jsonParser = new Gson();

  public PlayerState() { }

  public static PlayerState fromJson(String jsonData) {
    return jsonParser.fromJson(jsonData, PlayerState.class);
  }

  public PlayerState(String csvData) {
    csvData = csvData.substring(5);
    String[] data = csvData.split(",");
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
    shieldOn = Boolean.parseBoolean(data[10]);
    currentPower = Float.parseFloat(data[11]);
    timestamp = Long.parseLong(data[12]);
  }

  public PlayerState(byte[] data) {
    generationId = decodeString(get(data, 0, 4));
    shipId = decodeInt(get(data, 4, 4));
    x = decodeFloat(get(data, 8, 4));
    y = decodeFloat(get(data, 12, 4));
    angle = decodeFloat(get(data, 16, 4));
    speedX = decodeFloat(get(data, 20, 4));
    speedY = decodeFloat(get(data, 24, 4));
    angularSpeed = decodeFloat(get(data, 28, 4));
    health = decodeFloat(get(data, 32, 4));
    isUnderControl = decodeBoolean(get(data, 36, 1));
    shieldOn = decodeBoolean(get(data, 37, 1));
    currentPower = decodeFloat(get(data, 38, 4));
    timestamp = decodeLong(get(data, 42, 8));
  }

  public static boolean isInstance(byte[] data) {
    return data.length == length;
  }

  public byte[] compress() {
    return ByteBuffer.allocate(length)
            .put(generationId.getBytes(StandardCharsets.UTF_8))
            .putInt(shipId)
            .putFloat(x)
            .putFloat(y)
            .putFloat(angle)
            .putFloat(speedX)
            .putFloat(speedY)
            .putFloat(angularSpeed)
            .putFloat(health)
            .put(isUnderControl ? (byte) 1 : (byte) 0)
            .put(shieldOn ? (byte) 1 : (byte) 0)
            .putFloat(currentPower)
            .putLong(timestamp)
            .array();
  }

  public String toJson() {
    return jsonParser.toJson(this);
  }

  public String toCsv() {
    return "state" +
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
            shieldOn + "," +
            currentPower + "," +
            timestamp;
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
  '{"generationId":"defender0202797818","shipId":"defender","x":-10.836355,"y":53.327827,"angle":0.81490326,"speedX":1.8270377,"speedY":6.214534,"angularSpeed":-0.0019457892,"health":44.9,"isUnderControl":true,"currentPower":0.0041643687,"timestamp":1650204282511}'

  csv:

  'state[defender0202797818,defender,-10.836355,53.327827,0.81490326,1.8270377,6.214534,-0.0019457892, 44.9,false,4.1643687E-4,1650204282511]'

  final 50 symbols:

  '~@Eg?-a?BUO??P?????_@??v???Bf9?U6�?7?�?'

  */

  public static void main(String[] a) {
    PlayerState state = new PlayerState();
    state.generationId = CalculateUtils.uid();
    state.shipId = 3;
    state.x = 9231112.3112131f;
    state.y = -0.0f;
    state.angle = 2.423132f;
    state.speedX = -24.1332f;
    state.speedY = 0.923f;
    state.angularSpeed = -0.331091f;
    state.health = 32.4129f;
    state.isUnderControl = true;
    state.shieldOn = false;
    state.currentPower = 0.9312041f;
    state.timestamp = System.currentTimeMillis();

    try {
      System.out.println(state);
      System.out.println(new PlayerState(state.compress()));
    } catch (Exception e) {
      e.printStackTrace();
    }

    parseBenchmark(state);
  }


  private static void compressBenchmark(PlayerState state) {
    try {
      long iterations = 0;
      System.out.println("compress csv: ");
      long t0 = System.currentTimeMillis();
      for (int j = 1; j < 11; j++) {
        for (long i = 0; i < 5000 * j * j; i++) {
          state.toCsv();
          iterations++;
        }
        long t = System.currentTimeMillis();
        System.out.println("{" + iterations + "," + (t - t0) + "},");
      }
      System.out.println();

      iterations = 0;
      System.out.println("compress json: ");
      t0 = System.currentTimeMillis();
      for (int j = 1; j < 11; j++) {
        for (long i = 0; i < 5000 * j * j; i++) {
          state.toJson();
          iterations++;
        }
        long t = System.currentTimeMillis();
        System.out.println("{" + iterations + "," + (t - t0) + "},");
      }
      System.out.println();

      iterations = 0;
      System.out.println("compress custom: ");
      System.out.println(new String(state.compress()));
      t0 = System.currentTimeMillis();
      for (int j = 1; j < 11; j++) {
        for (long i = 0; i < 5000 * j * j; i++) {
          state.compress();
          iterations++;
        }
        long t = System.currentTimeMillis();
        System.out.println("{" + iterations + "," + (t - t0) + "},");
      }
      System.out.println();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void parseBenchmark(PlayerState state) {
    try {
      String csv = state.toCsv();
      String json = state.toJson();
      byte[] custom = state.compress();

      System.out.println("json:   " + json);
      System.out.println("csv:    " + csv);
      System.out.println("custom: " + new String(custom));

      long iterations = 0;
      System.out.println("parse json: ");
      long t0 = System.currentTimeMillis();
      for (int j = 1; j < 11; j++) {
        for (long i = 0; i < 5000 * j * j; i++) {
          fromJson(json);
          iterations++;
        }
        long t = System.currentTimeMillis();
        System.out.println("{" + iterations + "," + (t - t0) + "},");
      }
      System.out.println();

      iterations = 0;
      System.out.println("parse custom: ");
      t0 = System.currentTimeMillis();
      for (int j = 1; j < 11; j++) {
        for (long i = 0; i < 5000 * j * j; i++) {
          new PlayerState(custom);
          iterations++;
        }
        long t = System.currentTimeMillis();
        System.out.println("{" + iterations + "," + (t - t0) + "},");
      }
      System.out.println();

      iterations = 0;
      System.out.println("parse csv: ");
      t0 = System.currentTimeMillis();
      for (int j = 1; j < 11; j++) {
        for (long i = 0; i < 5000 * j * j; i++) {
          new PlayerState(csv);
          iterations++;
        }
        long t = System.currentTimeMillis();
        System.out.println("{" + iterations + "," + (t - t0) + "},");
      }
      System.out.println();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
