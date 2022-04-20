package com.mikilangelo.abysmal.screens.game.enemies.online;

import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_HEIGHT;
import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_WIDTH;
import static com.mikilangelo.abysmal.screens.game.GameScreen.camera;
import static com.mikilangelo.abysmal.screens.game.GameScreen.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.mikilangelo.abysmal.screens.game.enemies.EnemiesProcessor;
import com.mikilangelo.abysmal.screens.game.enemies.Enemy;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.AsteroidCrashed;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.DataPackage;
import com.mikilangelo.abysmal.shared.ShipDefinitions;
import com.mikilangelo.abysmal.shared.repositories.AsteroidsRepository;
import com.mikilangelo.abysmal.shared.repositories.ExplosionsRepository;
import com.mikilangelo.abysmal.shared.repositories.LasersRepository;
import com.mikilangelo.abysmal.screens.game.actors.ship.Ship;
import com.mikilangelo.abysmal.screens.game.actors.ship.Laser;
import com.mikilangelo.abysmal.screens.game.objectsData.DestroyableObjectData;
import com.mikilangelo.abysmal.screens.game.objectsData.ShipData;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.PlayerState;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.ShotData;
import com.mikilangelo.abysmal.shared.tools.CalculateUtils;
import com.mikilangelo.abysmal.screens.game.uiElements.Radar;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

public class UdpClient implements EnemiesProcessor {

  private final InetAddress address;
  private final int port;
  private final DatagramSocket client;

  private final Array<Player> players = new Array<>();
  private final AtomicBoolean isStopped = new AtomicBoolean(false);
  private byte[] output;
  private DatagramPacket outputPacket;
  private final DatagramPacket inputPacket;
  private byte missedFrames = 0;
  private final PlayerState state = new PlayerState();
  private final SendingThread sendingThread;
  private final Thread receiveThread;

  private float playerX;
  private float playerY;

  public UdpClient(String ip, int port) throws IOException {
    this.port = port;
    address = InetAddress.getByName(ip);
    this.client = new DatagramSocket();
    inputPacket = new DatagramPacket(new byte[256], 256);
    sendingThread = new SendingThread();
    receiveThread = new Thread(() -> {
      while (!isStopped.get()) {
        try {
          client.receive(inputPacket);
          if (isStopped.get()) {break;}
          handleData(trimPackage(inputPacket.getData()));
          inputPacket.setData(new byte[256]);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
      this.client.close();
    });
    receiveThread.start();
    sendingThread.start();
  }

  private byte[] trimPackage(byte[] data) {
    int i = (short) data.length - 1;
    while (i >= 0 && data[i] == 0) {
      i--;
    }
    return Arrays.copyOf(data, i+1);
  }

  @Override
  public Ship getNearestEnemy(float playerX, float playerY) {
    return null;
  }

  @Override
  public void generateEnemies(Ship ship) {
    state.generationId = ship.generationId;
    state.shipId = ship.def.id;
    playerX = state.x = ship.x;
    playerY = state.y = ship.y;
    state.health = 999;
    state.speedX = state.speedY = state.angularSpeed = state.angle = 0;
    state.timestamp = System.currentTimeMillis();
    try {
      output = state.compress();
      outputPacket = new DatagramPacket(output, output.length, address, port);
      client.send(outputPacket);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void handleData(byte[] dataPackage) {
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
          if (player.generationId.length() > 1) {
            for (int i = 0; i < players.size; i++) {
              if (CalculateUtils.distance(player.x, player.y, players.get(i).ship.x, players.get(i).ship.y) < 2) {
                return;
              }
            }
            Ship ship = new Ship(ShipDefinitions.get(player.shipId),
                    player.x, player.y, false, playerX, playerY);
            players.add(new Player(ship, player.generationId, false));
          }
        }
      }
      else if (ShotData.isInstance(dataPackage)) {
        System.out.println("recieved shot data: ");
        ShotData shotData = new ShotData(dataPackage);
        System.out.println(shotData);
        for (Player p : players) {
          if (p.generationId.equals(shotData.generationId)) {
            p.shot(shotData);
            return;
          }
        }
      }
      else if (AsteroidCrashed.isInstance(dataPackage)) {
        AsteroidCrashed crashData = new AsteroidCrashed(dataPackage);
        AsteroidsRepository.handleCrash(crashData);
      }
    } catch (NumberFormatException e) {
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
      state.isUnderControl = player.isPowerApplied;
      state.currentPower = player.currentPower;
      player.isPowerApplied = false;
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
    final float w = SCREEN_WIDTH * camera.zoom;
    final float h = SCREEN_HEIGHT * camera.zoom;
    final float x = camera.X;
    final float y = camera.Y;
    for (int i = 0; i < players.size; i++) {
      players.get(i).draw(batch, delta, x, y, w, h);
    }
  }

  @Override
  public void drawAtRadar(Batch batch, Radar radar) {
    for (int i = 0; i < players.size; i++) {
      radar.drawEnemy(batch, players.get(i).ship.x, players.get(i).ship.y);
    }
  }

  @Override
  public void shot(ShotData shotData) {
    shotData.generationId = state.generationId;
    shotData.timestamp = TimeUtils.millis();
    sendingThread.sendData(shotData);
  }

  @Override
  public void explodeAsteroid(long asteroidId, float x, float y, float angle) {
    AsteroidCrashed data = new AsteroidCrashed();
    data.asteroidId = asteroidId;
    data.x = x;
    data.y = y;
    data.angle = angle;
    data.timestamp = System.currentTimeMillis();
    sendingThread.sendData(data);
  }

  @Override
  public void dispose() {
    isStopped.set(true);
    players.clear();
    receiveThread.interrupt();
    sendingThread.interrupt();
    state.health = 0;
    try {
      output = state.compress();
      outputPacket.setData(output, 0, output.length);
      client.send(outputPacket);
    } catch (IOException | SecurityException e) {
      e.printStackTrace();
    }
  }

  private class Player extends Enemy {
    private final String generationId;
    private boolean isPowerApplied = false;
    private int badPackages = 10;
    private PlayerState lastState;
    private boolean isDead = false;
    private float currentPower = 0.5f;

    public Player(Ship ship, String id, boolean friendly) {
      super(ship);
      this.generationId = id;
      lastState = new PlayerState();
      lastState.speedX = lastState.speedY = 0;
      ship.generationId = friendly ? state.generationId : id;
      ship.createBody(world);
    }

    public boolean isDead(float playerX, float playerY, float delta) {
      if (isDead) return true;
      ship.distance = CalculateUtils.distance(playerX, playerY, ship.x, ship.y);
      if (ship.distance < 50 && !world.isLocked()) {
        ship.move(delta);
        if (isPowerApplied) {
          ship.kak();
          this.currentPower = this.currentPower * 0.96f + 0.04f;
        }
        ship.currentPower = this.currentPower;
      }
      if (((ShipData) ship.body.getUserData()).health <= -30) {
        if (ship.distance < 100) {
          ExplosionsRepository.addShipExplosion(
                  ship.x, ship.y, 1 - ship.distance * 0.01f,
                  (ship.x - playerX) / ship.distance );
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
          isPowerApplied = data.isUnderControl;
          if (badPackages < 5 && data.health != 0) {
            if (CalculateUtils.distance(lastState.x + lastState.speedX * deltaTime,
                    lastState.y + lastState.speedY * deltaTime,
                    data.x, data.y) > Math.hypot(lastState.speedX, lastState.speedY) * compareTime * (1 + badPackages) ||
                    (CalculateUtils.distance(data.speedX, data.speedY, lastState.speedX, lastState.speedY) > (0.7f + compareTime) * (2 + badPackages) &&
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
                ExplosionsRepository.addShipExplosion(
                        ship.x, ship.y, 1 - ship.distance * 0.01f,
                        (ship.x - playerX) / ship.distance );
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
          ship.x = data.x;
          ship.y = data.y;
          ((ShipData)ship.body.getUserData()).health = data.health;
          if (data.isUnderControl) {
            this.currentPower = 0.5f * (this.currentPower + 0.2f + data.currentPower * 0.8f);
          }

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

    public void shot(ShotData shotData) {
      Gdx.app.postRunnable(() -> {
        if (shotData.withSound) {
          ship.playShotSound(shotData.gunId);
        }
        Laser l = new Laser(
                shotData.gunId < 0 ? ship.def.laserDefinition :
                        ship.def.turretDefinitions.get(shotData.gunId).laserDefinition,
                shotData,
                ship.bodyId
        );
        if (shotData.gunId < 0) {
          LasersRepository.addSimple(l);
        } else {
          LasersRepository.addTurret(l);
        }
      });
    }
  }

  private class SendingThread extends Thread {

    private final Vector<DataPackage> sequence = new Vector<>();
    private final AtomicBoolean needToSendState = new AtomicBoolean(false);

    public void sendData(DataPackage data) {
      sequence.add(data);
      if (sequence.size() > 20) {
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
          //long t = System.currentTimeMillis();
          try {
            output = state.compress();
            outputPacket.setData(output, 0, output.length);
            client.send(outputPacket);
            needToSendState.set(false);
            //System.out.println("\noriginal: " + state);
            //System.out.println("decoded:  " + new PlayerState(output));
          } catch (ArrayIndexOutOfBoundsException | IOException e) {
            e.printStackTrace();
          }
          //System.out.println("send packet took + " + (System.currentTimeMillis() - t) + "ms.");
        }
        if (sequence.size() > 0) {
          try {
            output = sequence.get(0).compress();
            outputPacket.setData(output, 0, output.length);
            client.send(outputPacket);
            System.out.println("\noriginal:\n" + Arrays.toString(output));
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
