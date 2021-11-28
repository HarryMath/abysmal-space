package com.mikilangelo.abysmal.enemies.remote;

import static com.mikilangelo.abysmal.ui.screens.GameScreen.SCREEN_WIDTH;
import static com.mikilangelo.abysmal.ui.screens.GameScreen.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mikilangelo.abysmal.components.ShipDefinitions;
import com.mikilangelo.abysmal.components.repositories.ExplosionsRepository;
import com.mikilangelo.abysmal.enemies.EnemiesProcessor;
import com.mikilangelo.abysmal.models.game.Ship;
import com.mikilangelo.abysmal.models.objectsData.DestroyableObjectData;
import com.mikilangelo.abysmal.models.objectsData.ShipData;
import com.mikilangelo.abysmal.models.sending.PlayerDataRequest;
import com.mikilangelo.abysmal.models.sending.PlayerInitializingData;
import com.mikilangelo.abysmal.models.sending.PlayerStateData;
import com.mikilangelo.abysmal.models.sending.ShotData;
import com.mikilangelo.abysmal.tools.Geometry;
import com.mikilangelo.abysmal.ui.Radar;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

public class UdpClient implements EnemiesProcessor {

  private final InetAddress address;
  private static final int port = 8080;

  private final Array<Player> players = new Array<>();
  private final DatagramSocket client;
  private final AtomicBoolean isStopped = new AtomicBoolean(false);
  private final Gson parser = new Gson();
  private byte[] output;
  private DatagramPacket outputPacket;
  private final DatagramPacket inputPacket;
  private int missedFrames = 0;
  private final PlayerStateData state = new PlayerStateData();
  private final PlayerInitializingData initData = new PlayerInitializingData();
  private final SendingThread sendingThread;
  private final Thread receiveThread;

  private float playerX;
  private float playerY;

  public UdpClient() throws IOException {
    this.client = new DatagramSocket();
//    address = InetAddress.getByName("localhost");
//    address = InetAddress.getByName("37.214.29.33");
    address = InetAddress.getByName("13.53.170.78");
    inputPacket = new DatagramPacket(new byte[512], 512);
    sendingThread = new SendingThread();
    receiveThread = new Thread(() -> {
      while (!isStopped.get()) {
        try {
          client.receive(inputPacket);
          handleData(new String(inputPacket.getData(), 0, inputPacket.getLength()));
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
      this.client.close();
    });
    receiveThread.start();
    sendingThread.start();
  }

  @Override
  public Ship getNearestEnemy(float playerX, float playerY) {
    return null;
  }

  @Override
  public void generateEnemies(Ship ship) {
    initData.generationId = ship.generationId;
    initData.shipId = ship.definition.name;
    playerX = initData.x = ship.x;
    playerY = initData.y = ship.y;
    try {
      output = initData.toString().getBytes();
      outputPacket = new DatagramPacket(output, output.length, address, port);
      client.send(outputPacket);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void handleData(String json) {
    try {
      if (PlayerStateData.isInstance(json)) {
        PlayerStateData playerState = parser.fromJson(json, PlayerStateData.class);
        if (!playerState.g.equals(state.g)) {
          boolean playerFound = false;
          for (int i = 0; i < players.size; i++) {
            if (players.get(i).generationId.equals(playerState.g)) {
              playerFound = true;
              players.get(i).update(playerState);
              break;
            }
          }
          if (!playerFound && playerState.g.length() > 10) {
            PlayerDataRequest request = new PlayerDataRequest();
            request.playerId = playerState.g;
            sendingThread.sendData(request);
          }
        }
      }
      else if (ShotData.isInstance(json)) {
        ShotData shotData = parser.fromJson(json, ShotData.class);
        for (Player p : players) {
          if (p.generationId.equals(shotData.g)) {
            p.shot(shotData);
            return;
          }
        }
      }
      else if (PlayerInitializingData.isInstance(json)) {
        PlayerInitializingData newPlayer = parser.fromJson(json, PlayerInitializingData.class);
        if (!newPlayer.generationId.equals(state.g)) {
          for (int i = 0; i < players.size; i++) {
            if (Geometry.distance(newPlayer.x, newPlayer.y, players.get(i).ship.x, players.get(i).ship.y) < 2) {
              return;
            }
          }
          Ship ship = new Ship(
                  ShipDefinitions.getShipDefinition(newPlayer.shipId),
                  newPlayer.x, newPlayer.y, false, playerX, playerY);
          players.add(new Player(ship, newPlayer.generationId, false));
        }
      }
    } catch (JsonSyntaxException | NumberFormatException e) {
      System.out.println("parse error: ");
      System.out.println(json + "\n");
    }
  }

  @Override
  public void process(final Ship player, final float delta) {
    missedFrames++;
    if (missedFrames > 4) {
      missedFrames = 0;
      state.g = player.generationId;
      state.x = playerX = player.x;
      state.y = playerY = player.y;
      state.aX = player.body.getLinearVelocity().x;
      state.aY = player.body.getLinearVelocity().y;
      state.aA = player.body.getAngularVelocity();
      state.a = player.angle;
      state.t = TimeUtils.millis();
      state.c = player.underControl;
      state.h = ((DestroyableObjectData) player.body.getUserData()).getHealth();
      sendingThread.sendStateData();
    }
    for (int i = 0; i < players.size; i++) {
      if ( players.get(i).isDead(player.x, player.y, delta) ) {
        players.removeIndex(i--);
      }
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
  public void shot() {
    ShotData shotData = new ShotData();
    shotData.g = state.g;
    shotData.x = playerX;
    shotData.y = playerY;
    shotData.tur = false;
    shotData.iX = state.aX;
    shotData.iY = state.aY;
    shotData.t = TimeUtils.millis();
    shotData.a = state.a;
    sendingThread.sendData(shotData);
  }

  @Override
  public void dispose() {
    isStopped.set(true);
    sendingThread.interrupt();
    receiveThread.interrupt();
    players.clear();
  }

  private class Player {
    private float distance;
    private final String generationId;
    private boolean underControl = false;
    private int badPackages = 10;
    private PlayerStateData lastState;
    final Ship ship;

    public Player(Ship ship, String id, boolean friendly) {
      this.ship = ship;
      this.generationId = id;
      lastState = new PlayerStateData();
      lastState.aX = lastState.aY = 0;
      ship.generationId = friendly ? initData.generationId : id;
      ship.createBody(world);
    }

    public boolean isDead(float playerX, float playerY, float delta) {
      distance = Geometry.distance(playerX, playerY, ship.x, ship.y);
      if (distance < 50 && !world.isLocked()) {
        ship.move(delta);
        if (underControl) {
          ship.kak();
        }
      }
      if (((ShipData) ship.body.getUserData()).health <= 0) {
        if (ship.distance < 100) {
          ExplosionsRepository.addShipExplosion(ship.x, ship.y, 1 - ship.distance / 100);
        }
        ship.destroy(world);
        return true;
      }
      return false;
    }

    public void update(final PlayerStateData data) {
      Gdx.app.postRunnable(() -> {
        if (!world.isLocked() && !isStopped.get() && data.t > lastState.t) {
//            final float deltaTime = TimeUtils.millis() - data.t / 1000f;
          final float deltaTime = (data.t - lastState.t) / 1000f;
          final float compareTime = 0.5f + deltaTime * 2f;
          underControl = data.c;
          if (badPackages < 5) {
            if (Geometry.distance(
                    lastState.x + lastState.aX * deltaTime,
                    lastState.y + lastState.aY * deltaTime,
                    data.x, data.y) > Math.hypot(lastState.aX, lastState.aY) * compareTime * (1 + badPackages) ||
                    Geometry.distance(data.aX, data.aY, lastState.aX, lastState.aY) > (0.8f + compareTime) * (2.3f + badPackages)
            ) {
              badPackages++;
              System.out.println("\nbad package " + badPackages);
              System.out.println("delta: " + deltaTime + "s; compare: " + compareTime);
              System.out.println("prev: [x: " + lastState.x + ", y: " + lastState.y + ", ax: " + lastState.aX + ", ay: " + lastState.aY + "]");
              System.out.println("expc: [x: " + (lastState.x + lastState.aX * deltaTime) + ", y: " + (lastState.y + lastState.aY * deltaTime) + ", ax: " + lastState.aX + ", ay: " + lastState.aY + "]");
              System.out.println("got:  [x: " + data.x + ", y: " + data.y + ", ax: " + data.aX + ", ay: " + data.aY + "]");
              return;
            } else  {
              badPackages = 0;
            }
          } else {
            badPackages = 0;
          }
          lastState = data;
          ship.body.setLinearVelocity(data.aX * 0.97f, data.aY * 0.97f);
          ship.body.setAngularVelocity(data.aA * 0.97f);
          ship.body.setTransform(data.x, data.y, data.a);
          ((ShipData)ship.body.getUserData()).health = data.h;

//            final float updatePower = 0.03f + deltaMillis / 500f;
//            ship.body.setLinearVelocity(data.aX * 0.95f, data.aY * 0.95f);
//            ship.body.setAngularVelocity(data.aA * 0.95f);
//            ship.body.setTransform(
//                    (ship.body.getPosition().x + updatePower * (data.x + data.aX * deltaTime)) / (updatePower + 1),
//                    (ship.body.getPosition().y + updatePower * (data.y + data.aY * deltaTime)) / (updatePower + 1),
//                    Geometry.avgAngle(ship.angle, 1, (data.a + data.aA * deltaTime), updatePower));
//            ((ShipData)ship.body.getUserData()).health = data.h;
        }
      });
    }

    public void draw(Batch batch, float delta) {
      if (distance < SCREEN_WIDTH * 3) {
        ship.draw(batch, delta);
      }
    }

    public void shot(ShotData shotData) {
      Gdx.app.postRunnable(() -> {
        if (shotData.tur) {

        } else {
          ship.shotDirectly(0.1f, shotData.x, shotData.y, shotData.iX, shotData.iY, 0);
        }
      });
    }
  }

  private class SendingThread extends Thread {

    private final Vector<Object> sequence = new Vector<>();
    private final AtomicBoolean needToSendState = new AtomicBoolean(false);

    public void sendData(Object data) {
      sequence.add(data);
      if (sequence.size() > 10) {
        sequence.remove(0);
      }
    }

    public void sendStateData() {
      needToSendState.set(true);
    }

    @Override
    public void run() {
      while (!isStopped.get()) {
        if (needToSendState.get()) {
          try {
            output = state.toString().getBytes();
            outputPacket.setData(output, 0, output.length);
            client.send(outputPacket);
            needToSendState.set(false);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        if (sequence.size() > 0) {
          try {
            output = sequence.get(0).toString().getBytes();
            outputPacket.setData(output, 0, output.length);
            client.send(outputPacket);
          } catch (IOException e) {
            e.printStackTrace();
          }
          sequence.remove(0);
        }
      }
      System.out.println("sending thread ended");
    }
  }
}
