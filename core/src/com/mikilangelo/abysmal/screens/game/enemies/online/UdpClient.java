package com.mikilangelo.abysmal.screens.game.enemies.online;

import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_HEIGHT;
import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_WIDTH;
import static com.mikilangelo.abysmal.screens.game.GameScreen.camera;
import static com.mikilangelo.abysmal.screens.game.GameScreen.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.mikilangelo.abysmal.screens.game.enemies.EnemiesProcessor;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.AsteroidCrashed;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.DataPackage;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.DeathPackage;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.SimplifiedState;
import com.mikilangelo.abysmal.shared.ShipDefinitions;
import com.mikilangelo.abysmal.shared.repositories.AsteroidsRepository;
import com.mikilangelo.abysmal.screens.game.actors.ship.Ship;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.PlayerState;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.ShotData;
import com.mikilangelo.abysmal.screens.game.uiElements.Radar;
import com.mikilangelo.abysmal.shared.tools.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

public class UdpClient extends UdpProcessor implements EnemiesProcessor {

  private final InetAddress address;
  private final long timeCorrection;
  private final int port;
  private final DatagramSocket client;

  private final Array<Player> players = new Array<>();
  private final SendingThread sendingThread;

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
          handleData(Arrays.copyOf(inputPacket.getData(), inputPacket.getLength()));
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

  private double totalDelta = 0;
  private long totalPackages = 0;

  private void handleData(byte[] dataPackage) {
    try {
      if (SimplifiedState.isInstance(dataPackage)) {
        SimplifiedState player = new SimplifiedState(dataPackage);
        if (!player.generationId.equals(state.generationId)) {
          for (int i = 0; i < players.size; i++) {
            if (players.get(i).generationId.equals(player.generationId)) {
              players.get(i).update(player, timestamp());
              return;
            }
          }
          if (isNewPlayer(player.generationId)) {
            Ship ship = new Ship(ShipDefinitions.get(player.shipId),
                    player.x, player.y, false, playerX, playerY);
            Gdx.app.postRunnable(() -> {
              ship.createBody(world);
              players.add(new Player(ship, player.generationId));
            });
          }
        }
      }
      else if (PlayerState.isInstance(dataPackage)) {
        PlayerState player = new PlayerState(dataPackage);
        if (!player.generationId.equals(state.generationId)) {
          for (int i = 0; i < players.size; i++) {
            if (players.get(i).generationId.equals(player.generationId)) {
              totalDelta += players.get(i).update(player, timestamp());
              totalPackages += 1;
              if (totalPackages > 1000) {
                Logger.log(
                        this,
                        "handleData",
                        "avg delta is " + (totalDelta / totalPackages * 1000) + " ms"
                );
              }
              return;
            }
          }
          if (isNewPlayer(player.generationId)) {
            Ship ship = new Ship(ShipDefinitions.get(player.shipId),
                    player.x, player.y, false, playerX, playerY);
            Gdx.app.postRunnable(() -> {
              ship.createBody(world);
              players.add(new Player(ship, player.generationId));
            });
          }
        }
      }
      else if (ShotData.isInstance(dataPackage)) {
        ShotData shotData = new ShotData(dataPackage);
        for (Player p : players) {
          if (p.generationId.equals(shotData.generationId)) {
            p.shot(shotData, timestamp());
            return;
          }
        }
      }
      else if (AsteroidCrashed.isInstance(dataPackage)) {
        AsteroidCrashed crashData = new AsteroidCrashed(dataPackage);
        AsteroidsRepository.handleCrash(crashData);
      }
      else if (DeathPackage.isInstance(dataPackage)) {
        String genIs = new DeathPackage(dataPackage).generationId;
        for (int i = 0; i < players.size; i++) {
          if (players.get(i).generationId.equals(genIs)) {
            players.get(i).makeDead();
            return;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      Logger.log(this,"handleData", "error parsing package: " + Arrays.toString(dataPackage));
    }
  }

  @Override
  public void process(final Ship player, final float delta) {
    missedFrames++;
    if (player.bodyData.health <= 0) {
      sendingThread.sendDeathPackage();
    }
    else if (missedFrames > 2) {
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
      state.health = player.bodyData.health;
      sendingThread.sendStateData();
    }
    long timestamp = timestamp();
    for (int i = 0; i < players.size; i++) {
      if ( players.get(i).isDead(player.x, player.y, delta, timestamp) ) {
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
  public void drawAtRadar(Batch batch, Radar radar, float cameraRotation) {
    for (int i = 0; i < players.size; i++) {
      radar.drawEnemy(batch, players.get(i).ship.x, players.get(i).ship.y, cameraRotation);
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
    super.dispose();
    isStopped.set(true);
    players.clear();
    receiveThread.interrupt();
    sendingThread.interrupt();
    players.clear();
    Logger.log(this, "dispose", "closed");
  }

  private long timestamp() {
    return System.currentTimeMillis() + timeCorrection;
  }

  private class SendingThread extends Thread {

    private final Vector<DataPackage> sequence = new Vector<>();
    private final AtomicBoolean needToSendState = new AtomicBoolean(false);
    private final AtomicBoolean isDead = new AtomicBoolean(false);

    public void sendData(DataPackage data) {
      sequence.add(data);
      if (sequence.size() > 20) {
        sequence.remove(0);
      }
    }

    public void sendDeathPackage() {
      isDead.set(true);
      needToSendState.set(true);
    }

    public void sendStateData() {
      needToSendState.set(true);
    }

    @Override
    public void run() {
      while (!isStopped.get()) {
        if (needToSendState.get()) {
          try {
            output = isDead.get() ?
                    new DeathPackage(state.generationId).compress() :
                    state.compress();
            outputPacket.setData(output, 0, output.length);
            client.send(outputPacket);
            needToSendState.set(false);
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
      Logger.log(this, "run", "ended");
    }
  }
}
