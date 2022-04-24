package com.mikilangelo.abysmal.screens.game.enemies.online;

import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_HEIGHT;
import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_WIDTH;
import static com.mikilangelo.abysmal.screens.game.GameScreen.camera;
import static com.mikilangelo.abysmal.screens.game.GameScreen.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
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
import com.mikilangelo.abysmal.screens.game.enemies.online.data.PlayerState;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.ShotData;
import com.mikilangelo.abysmal.shared.tools.CalculateUtils;
import com.mikilangelo.abysmal.screens.game.uiElements.Radar;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

public class UdpClient implements EnemiesProcessor {

  private final InetAddress address;
  private final long timeCorrection;
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

  public UdpClient(String ip, int port, long timeCorrection) throws IOException {
    this.port = port;
    this.timeCorrection = timeCorrection;
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
    state.health = ship.def.health;
    state.speedX = state.speedY = state.angularSpeed = state.angle = 0;
    state.timestamp = timestamp();
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
            Gdx.app.postRunnable(() -> {
              ship.createBody(world);
              players.add(new Player(ship, player.generationId, false));
            });
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
      else if (AsteroidCrashed.isInstance(dataPackage)) {
        AsteroidCrashed crashData = new AsteroidCrashed(dataPackage);
        AsteroidsRepository.handleCrash(crashData);
      }
    } catch (Exception e) {
      e.printStackTrace();
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
      state.timestamp = timestamp();
      state.isUnderControl = player.isPowerApplied;
      state.shieldOn = player.shieldOn;
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
    shotData.timestamp = timestamp();
    sendingThread.sendData(shotData);
  }

  @Override
  public void explodeAsteroid(long asteroidId, float x, float y, float angle) {
    AsteroidCrashed data = new AsteroidCrashed();
    data.asteroidId = asteroidId;
    data.x = x;
    data.y = y;
    data.angle = angle;
    data.timestamp = timestamp();
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
    private long lastUpdateTimeStamp = 0; // timestamp of the latest package
    private long lastStateTimeStamp = 0; // last time when update was
    private boolean isDead = false;
    private float currentPower = 0.5f;

    public Player(Ship ship, String id, boolean friendly) {
      super(ship);
      this.generationId = id;
      ship.generationId = friendly ? state.generationId : id;
    }

    public boolean isDead(float playerX, float playerY, float delta) {
      ship.distance = CalculateUtils.distance(playerX, playerY, ship.x, ship.y);
      if (
         isDead ||
         (ship.bodyData.health < -10 && timestamp() - lastUpdateTimeStamp > 500)
      ) {
        if (ship.distance < 100) {
          ExplosionsRepository.addShipExplosion(
                  ship.x, ship.y, 1 - ship.distance * 0.01f,
                  (ship.x - playerX) / ship.distance );
        }
        ship.destroy(world);
        return true;
      }
      if (ship.distance < 50) {
        ship.move(delta);
        if (isPowerApplied) {
          ship.kak();
          this.currentPower = this.currentPower * 0.96f + 0.04f;
        }
        ship.currentPower = this.currentPower;
      }
      return false;
    }

    public void update(final PlayerState data) {
      Gdx.app.postRunnable(() -> {
        if (!world.isLocked() && !isStopped.get() && data.timestamp > lastStateTimeStamp) {
          final long currentTime = timestamp();
          final float deltaTime = (currentTime - data.timestamp) * 0.001f;
          System.out.println("delta is: " + deltaTime + "s");
          isPowerApplied = data.isUnderControl;
          ship.bodyData.health = data.health;
          if (data.health <= 0) {
            this.isDead = true;
            return;
          }
          ship.setState(data, deltaTime * 0.7f);
          if (data.isUnderControl) {
            this.currentPower = 0.5f * (this.currentPower + 0.2f + data.currentPower * 0.8f);
          }
          lastUpdateTimeStamp = currentTime;
          lastStateTimeStamp = data.timestamp;
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
                ship.bodyId,
                (timestamp() - shotData.timestamp) * 0.001f
        );
        if (shotData.gunId < 0) {
          LasersRepository.addSimple(l);
        } else {
          LasersRepository.addTurret(l);
        }
      });
    }
  }

  private long timestamp() {
    return System.currentTimeMillis() + timeCorrection;
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
          //long t = now();
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
          //System.out.println("send packet took + " + (now() - t) + "ms.");
        }
        if (sequence.size() > 0) {
          try {
            output = sequence.get(0).compress();
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
