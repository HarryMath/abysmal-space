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
import com.mikilangelo.abysmal.components.repositories.ExplosionsRepository;
import com.mikilangelo.abysmal.enemies.EnemiesProcessor;
import com.mikilangelo.abysmal.models.game.Ship;
import com.mikilangelo.abysmal.models.objectsData.DestroyableObjectData;
import com.mikilangelo.abysmal.models.objectsData.ShipData;
import com.mikilangelo.abysmal.models.sending.PlayerState;
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
  private byte[] output;
  private DatagramPacket outputPacket;
  private final DatagramPacket inputPacket;
  private int missedFrames = 0;
  private final PlayerState state = new PlayerState();
  private final SendingThread sendingThread;
  private final Thread receiveThread;

  private float playerX;
  private float playerY;

  public UdpClient() throws IOException {
    this.client = new DatagramSocket();
//    address = InetAddress.getByName("localhost");
    address = InetAddress.getByName("13.53.170.78");
    inputPacket = new DatagramPacket(new byte[512], 512);
    sendingThread = new SendingThread();
    receiveThread = new Thread(() -> {
      while (!isStopped.get()) {
        try {
          client.receive(inputPacket);
          if (isStopped.get()) {break;}
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
    state.generationId = ship.generationId;
    state.shipName = ship.definition.name;
    playerX = state.x = ship.x;
    playerY = state.y = ship.y;
    state.health = 999;
    state.speedX = state.speedY = state.angularSpeed = state.angle = 0;
    state.timestamp = System.currentTimeMillis();
    try {
      output = state.toString().getBytes();
      outputPacket = new DatagramPacket(output, output.length, address, port);
      client.send(outputPacket);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void handleData(String dataPackage) {
    try {
      if (PlayerState.isInstance(dataPackage)) {
        PlayerState player = new PlayerState(dataPackage);
        if (!player.generationId.equals(state.generationId)) {
          for (int i = 0; i < players.size; i++) {
            if (players.get(i).generationId.equals(player.generationId)) {
              players.get(i).update(player);
              return;
            }
          }
          if (player.generationId.length() > 10) {
            for (int i = 0; i < players.size; i++) {
              if (Geometry.distance(player.x, player.y, players.get(i).ship.x, players.get(i).ship.y) < 2) {
                return;
              }
            }
            Ship ship = new Ship(ShipDefinitions.getShipDefinition(player.shipName),
                    player.x, player.y, false, playerX, playerY);
            players.add(new Player(ship, player.generationId, false));
          }
        }
      }
      else if (ShotData.isInstance(dataPackage)) {
        ShotData shotData = new ShotData(dataPackage);
        for (Player p : players) {
          if (p.generationId.equals(shotData.generationId)) {
            p.shot(shotData);
            return;
          }
        }
      }
    } catch (JsonSyntaxException | NumberFormatException e) {
      System.out.println("parse error: ");
      System.out.println(dataPackage + "\n");
    }
  }

  @Override
  public void process(final Ship player, final float delta) {
    missedFrames++;
    if (missedFrames > 2) {
      missedFrames = 0;
      state.generationId = player.generationId;
      state.x = playerX = player.x;
      state.y = playerY = player.y;
      state.speedX = player.body.getLinearVelocity().x;
      state.speedY = player.body.getLinearVelocity().y;
      state.angularSpeed = player.body.getAngularVelocity();
      state.angle = player.angle;
      state.timestamp = TimeUtils.millis();
      state.isUnderControl = player.underControl;
      player.underControl = false;
      state.health = ((DestroyableObjectData) player.body.getUserData()).getHealth();
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
    shotData.generationId = state.generationId;
    shotData.x = playerX;
    shotData.y = playerY;
    shotData.hasTurret = false;
    shotData.impulseX = state.speedX;
    shotData.impulseY = state.speedY;
    shotData.timestamp = TimeUtils.millis();
    shotData.angle = state.angle;
    sendingThread.sendData(shotData);
  }

  @Override
  public void dispose() {
    isStopped.set(true);
    players.clear();
    receiveThread.interrupt();
    sendingThread.interrupt();
    state.health = 0;
    output = state.toString().getBytes();
    outputPacket.setData(output, 0, output.length);
    try {
      client.send(outputPacket);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private class Player {
    private final String generationId;
    private boolean underControl = false;
    private int badPackages = 10;
    private PlayerState lastState;
    private boolean isDead = false;
    final Ship ship;

    public Player(Ship ship, String id, boolean friendly) {
      this.ship = ship;
      this.generationId = id;
      lastState = new PlayerState();
      lastState.speedX = lastState.speedY = 0;
      ship.generationId = friendly ? state.generationId : id;
      ship.createBody(world);
    }

    public boolean isDead(float playerX, float playerY, float delta) {
      if (isDead) return true;
      ship.distance = Geometry.distance(playerX, playerY, ship.x, ship.y);
      if (ship.distance < 50 && !world.isLocked()) {
        ship.move(delta);
        if (underControl) {
          ship.kak();
        }
      }
      if (((ShipData) ship.body.getUserData()).health <= -30) {
        if (ship.distance < 100) {
          ExplosionsRepository.addShipExplosion(ship.x, ship.y, 1 - ship.distance / 100);
        }
        this.isDead = true;
        ship.destroy(world);
        return true;
      }
      return false;
    }

    public void update(final PlayerState data) {
      Gdx.app.postRunnable(() -> {
        if (!world.isLocked() && !isStopped.get() && data.timestamp > lastState.timestamp) {
//            final float deltaTime = TimeUtils.millis() - data.t / 1000f;
          final float deltaTime = (data.timestamp - lastState.timestamp) / 1000f;
          final float compareTime = 0.3f + deltaTime * 1.5f;
          underControl = data.isUnderControl;
          if (badPackages < 5 && data.health != 0) {
            if (Geometry.distance(lastState.x + lastState.speedX * deltaTime,
                    lastState.y + lastState.speedY * deltaTime,
                    data.x, data.y) > Math.hypot(lastState.speedX, lastState.speedY) * compareTime * (1 + badPackages) ||
                    (Geometry.distance(data.speedX, data.speedY, lastState.speedX, lastState.speedY) > (0.7f + compareTime) * (2 + badPackages) &&
                            Math.abs(data.speedX) + Math.abs(data.speedY) > Math.abs(lastState.speedX) + Math.abs(lastState.speedY))
            ) {
              badPackages++;
              System.out.println("\nbad package " + badPackages);
              System.out.println("delta: " + deltaTime + "s; compare: " + compareTime);
              System.out.println("prev: [x: " + lastState.x + ", y: " + lastState.y + ", ax: " + lastState.speedX + ", ay: " + lastState.speedY + "]");
              System.out.println("expc: [x: " + (lastState.x + lastState.speedX * deltaTime) + ", y: " + (lastState.y + lastState.speedY * deltaTime) + ", ax: " + lastState.speedX + ", ay: " + lastState.speedY + "]");
              System.out.println("got:  [x: " + data.x + ", y: " + data.y + ", ax: " + data.speedX + ", ay: " + data.speedY + "]");
              return;
            } else  {
              badPackages = 0;
            }
          } else {
            badPackages = 0;
          }
          if (data.health <= 0) {
            if (!isDead) {
              if (ship.distance < 100) {
                ExplosionsRepository.addShipExplosion(ship.x, ship.y, 1 - ship.distance / 100);
              }
              ship.destroy(world);
              this.isDead = true;
            }
            return;
          }
          lastState = data;
          ship.body.setLinearVelocity(0, 0);
          ship.body.setLinearVelocity(data.speedX * 0.9f, data.speedY * 0.9f);
          ship.body.setAngularVelocity(data.angularSpeed * 0.9f);
          ship.body.setTransform(data.x, data.y, data.angle);
          ((ShipData)ship.body.getUserData()).health = data.health;

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
      if (ship.distance < SCREEN_WIDTH * 3) {
        ship.draw(batch, delta);
      }
    }

    public void shot(ShotData shotData) {
      Gdx.app.postRunnable(() -> {
        if (shotData.hasTurret) {

        } else {
          ship.shotDirectly(0.1f, shotData.x, shotData.y, shotData.impulseX, shotData.impulseY, 0);
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
