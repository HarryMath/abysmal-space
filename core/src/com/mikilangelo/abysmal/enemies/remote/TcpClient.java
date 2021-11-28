package com.mikilangelo.abysmal.enemies.remote;

import static com.mikilangelo.abysmal.ui.screens.GameScreen.SCREEN_WIDTH;
import static com.mikilangelo.abysmal.ui.screens.GameScreen.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mikilangelo.abysmal.components.ShipDefinitions;
import com.mikilangelo.abysmal.enemies.EnemiesProcessor;
import com.mikilangelo.abysmal.models.game.Ship;
import com.mikilangelo.abysmal.models.sending.PlayerDataRequest;
import com.mikilangelo.abysmal.models.sending.PlayerInitializingData;
import com.mikilangelo.abysmal.models.sending.PlayerStateData;
import com.mikilangelo.abysmal.tools.Geometry;
import com.mikilangelo.abysmal.ui.Radar;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TcpClient implements EnemiesProcessor {

  private final Array<Player> players = new Array<>();
  private final Socket client;
  private boolean isStopped = false;
  private final Gson parser = new Gson();
  private boolean sendingThreadBusy = false;
  private final DataOutputStream outputStream;
  private final DataInputStream inputStream;
  private int missedFrames = 0;
  private long sendTime = 0;
  private final byte[] bytes = new byte[4000];
  private final PlayerStateData state = new PlayerStateData();

  private float playerX;
  private float playerY;

  public TcpClient() throws IOException {
//    this.client = new Socket("abysmal-space.herokuapp.com", 80);
    this.client = new Socket("13.51.237.252", 8080);
//    this.client = new Socket("localhost", 8080);
    this.outputStream = new DataOutputStream(client.getOutputStream());
    this.inputStream = new DataInputStream(client.getInputStream());
    client.setTcpNoDelay(true);
  }
  
  @Override
  public Ship getNearestEnemy(float playerX, float playerY) {
    return null;
  }

  @Override
  public void generateEnemies(Ship ship) {
    PlayerInitializingData initData = new PlayerInitializingData();
    initData.generationId = ship.generationId;
    initData.shipId = ship.definition.name;
    playerX = initData.x = ship.x;
    playerY = initData.y = ship.y;
    try {
      outputStream.writeBytes(initData.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
    new Thread(new Runnable() {
      @Override
      public void run() {
        StringBuilder data = new StringBuilder();
        int bytesRead = 0;
        while (!isStopped && client.isConnected()) {
          try {
            while (bytesRead >= 0 && bytesRead < 100) {
              final int packageSize = inputStream.read(bytes);
              bytesRead += packageSize;
              if (bytesRead > 0 && bytesRead <= 4000) {
                String dataPackage = new String(bytes, 0, bytesRead);
                if (dataPackage.contains("oki")) {
                  System.out.println("ping: " + (TimeUtils.millis() - sendTime));
                  if (packageSize > 3) {
                    String normalData = dataPackage.replaceAll("oki", "");
                    data.append(normalData);
                    bytesRead -= (packageSize - normalData.length());
                  } else {
                    bytesRead -= 3;
                  }
                } else {
                  data.append(dataPackage);
                }
              }
            }
            int indexOfEnd = data.indexOf("}");
            if (indexOfEnd != -1) {
              int indexOfStart = data.indexOf("{");
              handleData(data.substring(indexOfStart, indexOfEnd + 1));
              data = new StringBuilder();
              bytesRead = 0;
            }
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        }
        System.out.println("thread ended");
      }
    }).start();
  }

  private void handleData(String json) {
    try {
      if (PlayerStateData.isInstance(json)) {
        PlayerStateData playerState = parser.fromJson(json, PlayerStateData.class);
        if (!playerState.g.equals(state.g)) {
          boolean playerFound = false;
          for (int i = 0; i < players.size; i++) {
            if (players.get(i).ship.generationId.equals(playerState.g)) {
              playerFound = true;
              players.get(i).update(playerState);
              break;
            }
          }
          if (!playerFound && !sendingThreadBusy && playerState.g.length() > 13) {
            PlayerDataRequest request = new PlayerDataRequest();
            request.playerId = playerState.g;
            try {
              outputStream.writeBytes(request.toString());
              outputStream.flush();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
      } else if (PlayerInitializingData.isInstance(json)) {
        System.out.println("\ninitialised data\n");
        PlayerInitializingData newPlayer = parser.fromJson(json, PlayerInitializingData.class);
        if (!newPlayer.generationId.equals(state.g)) {
          for (int i = 0; i < players.size; i++) {
            if (Geometry.distance(newPlayer.x, newPlayer.y, players.get(i).ship.x, players.get(i).ship.y) < 2) {
              System.out.println("error package");
              return;
            }
          }
          Ship ship = new Ship(
                  ShipDefinitions.getShipDefinition(newPlayer.shipId),
                  newPlayer.x, newPlayer.y, false, playerX, playerY);
          ship.createBody(world);
          ship.generationId = newPlayer.generationId;
          players.add(new Player(ship));
        }
      }
    } catch (JsonSyntaxException|NumberFormatException e) {
      System.out.println("parse error: ");
      System.out.println(json + "\n");
    }
  }

  @Override
  public void process(final Ship player, final float delta) {
    missedFrames++;
    if (!sendingThreadBusy && missedFrames > 3/* (dataSend || missedFrames > 120)*/) {
      sendingThreadBusy = true;
      missedFrames = 0;
      new Thread(() -> {
        state.g = player.generationId;
        state.x = playerX = player.x;
        state.y = playerY = player.y;
        state.aX = player.body.getLinearVelocity().x;
        state.aY = player.body.getLinearVelocity().y;
        state.aA = player.body.getAngularVelocity();
        state.a = player.angle;
        state.t = TimeUtils.millis();
        state.c = player.underControl;
        // state.health = ((DestroyableObjectData) player.body.getUserData()).getHealth();
        try {
          outputStream.writeBytes(state.toString());
          sendTime = TimeUtils.millis();
          outputStream.flush();
        } catch (IOException e) {
          e.printStackTrace();
        }
        player.underControl = false;
        sendingThreadBusy = false;
      }).start();
    }
    for (int i = 0; i < players.size; i++) {
      players.get(i).move(player.x, player.y, delta);
    }
  }

  @Override
  public void drawAll(Batch batch, float delta) {
    for (int i = 0; i < players.size; i++) {
      players.get(i).draw(batch, delta);
    }
  }

  @Override
  public void drawAtRadar(Batch batch, Radar radar, float playerX, float playerY) {
    for (int i = 0; i < players.size; i++) {
      radar.drawEnemy(batch, playerX, playerY, players.get(i).ship.x, players.get(i).ship.y);
    }
  }

  @Override
  public void dispose() {
    isStopped = true;
    try {
      this.client.close();
    } catch (IOException ignore) {}
  }

  @Override
  public void shot() { }

  private static class Player {
    private float distance;
    private float speed;
    private float speedAngle;
    private boolean underControl = false;
    private long lastUpdateTime = 0;
    Ship ship;

    public Player(Ship ship) {
      this.ship = ship;
    }

    public void move(float playerX, float playerY, float delta) {
      distance = Geometry.distance(playerX, playerY, ship.x, ship.y);
      if (distance < 50 && !world.isLocked()) {
        ship.move(delta);
        if (underControl) {
          ship.kak();
        }
      }
    }

    public void update(final PlayerStateData data) {
      Gdx.app.postRunnable(new Runnable() {
        @Override
        public void run() {
          if (!world.isLocked() && data.t > lastUpdateTime) {
            lastUpdateTime = data.t;
            final long deltaMillis = TimeUtils.millis() - data.t;
            System.out.println("delta: " + deltaMillis);
            underControl = data.c;
            final float deltaTime = (deltaMillis > 10 && deltaMillis < 3000) ?
                    (TimeUtils.millis() - data.t) / 1500f : 0;
//            final float currentSpeed = (float) Math.sqrt(data.aX * data.aX + data.aY * data.aA);
//            final float currentSpeedAngle = ship.body.getLinearVelocity().angle() * MathUtils.degreesToRadians;
//            final float currentSpeedProjection = currentSpeed *
//                    MathUtils.cos(currentSpeedAngle - ship.body.getAngle());
//            underControl = currentSpeedProjection > 0 &&
//                    speed * MathUtils.cos(speedAngle - ship.body.getAngle()) <= currentSpeedProjection;
//            speed = currentSpeed;
//            speedAngle = currentSpeedAngle;

            final float updatePower = 0.1f + deltaMillis / 500f;
            ship.body.setLinearVelocity(data.aX * 0.9f, data.aY * 0.9f);
            ship.body.setAngularVelocity(data.aA * 0.9f);
            ship.body.setTransform(
                    (ship.body.getPosition().x + updatePower * (data.x + data.aX * deltaTime)) / (updatePower + 1),
                    (ship.body.getPosition().y + updatePower * (data.y + data.aY * deltaTime)) / (updatePower + 1),
                    Geometry.avgAngle(ship.angle, 1, (data.a + data.aA * deltaTime), updatePower));
          }
        }
      });
    }

    public void draw(Batch batch, float delta) {
      if (distance < SCREEN_WIDTH * 3) {
        ship.draw(batch, delta);
      }
    }
  }
}
