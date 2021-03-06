package com.mikilangelo.abysmal.screens.game.enemies.online;

import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_HEIGHT;
import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_WIDTH;
import static com.mikilangelo.abysmal.screens.game.GameScreen.camera;
import static com.mikilangelo.abysmal.screens.game.GameScreen.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.mikilangelo.abysmal.screens.game.actors.ship.Ship;
import com.mikilangelo.abysmal.screens.game.enemies.EnemiesProcessor;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.AsteroidCrashed;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.BroadcastRequest;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.BroadcastResponse;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.DataPackage;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.DeathPackage;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.PlayerState;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.ShotData;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.SimplifiedState;
import com.mikilangelo.abysmal.screens.game.uiElements.Radar;
import com.mikilangelo.abysmal.shared.ShipDefinitions;
import com.mikilangelo.abysmal.shared.repositories.AsteroidsRepository;
import com.mikilangelo.abysmal.shared.tools.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

public class UdpServer extends UdpProcessor implements EnemiesProcessor {

  public static final int PORT = 7791;
  private final DatagramSocket server;
  public final long seed;

  private final Array<LocalPlayer> players = new Array<>();
  private final SendingThread sendingThread;
  private final Thread receiveThread;

  private float playerX;
  private float playerY;

  public UdpServer() throws IOException {
    this.server = new DatagramSocket(PORT);
    inputPacket = new DatagramPacket(new byte[256], 256);
    sendingThread = new SendingThread();
    receiveThread = new Thread(() -> {
      while (!isStopped.get()) {
        try {
          server.receive(inputPacket);
          if (isStopped.get()) {break;}
          handleData(
                  Arrays.copyOf(inputPacket.getData(), inputPacket.getLength()),
                  inputPacket.getAddress(),
                  inputPacket.getPort()
          );
          inputPacket.setData(new byte[256]);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
      this.server.close();
    });
    receiveThread.start();
    sendingThread.start();
    this.seed = 1 + System.currentTimeMillis() % 1000000;
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
  }


  private void handleData(byte[] dataPackage, InetAddress ip, int port) {
    sendingThread.sendFrom(new BroadcastPackage(dataPackage, ip, port));
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
              players.add(new LocalPlayer(ship, player.generationId, ip, port));
            });
          }
        }
      }
      else if (PlayerState.isInstance(dataPackage)) {
        PlayerState player = new PlayerState(dataPackage);
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
              players.add(new LocalPlayer(ship, player.generationId, ip, port));
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
      else if (BroadcastRequest.isInstance(dataPackage)) {
        sendingThread.sendTo(new DirectedPackage(new BroadcastResponse(seed), ip, port));
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
    for (short i = 0; i < players.size; i++) {
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
    for (short i = 0; i < players.size; i++) {
      players.get(i).draw(batch, delta, x, y, w, h);
    }
  }

  @Override
  public void drawAtRadar(Batch batch, Radar radar) {
    for (short i = 0; i < players.size; i++) {
      radar.drawEnemy(batch, players.get(i).ship.x, players.get(i).ship.y);
    }
  }

  @Override
  public void drawAtRadar(Batch batch, Radar radar, float cameraRotation) {
    for (short i = 0; i < players.size; i++) {
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
    isStopped.set(true);
    players.clear();
    receiveThread.interrupt();
    sendingThread.interrupt();
    players.clear();
  }

  private long timestamp() {
    return System.currentTimeMillis();
  }

  private static class LocalPlayer extends Player {

    public final InetAddress ip;
    public final int port;

    public LocalPlayer(Ship ship, String ip, String bodyId, InetAddress address, int port) {
      super(ship, ip, bodyId);
      this.ip = address;
      this.port = port;
    }

    public LocalPlayer(Ship ship, String ip, InetAddress address, int port) {
      this(ship, ip, ip, address, port);
    }
  }

  private class SendingThread extends Thread {

    private final Vector<DataPackage> sequence = new Vector<>();
    private final Vector<BroadcastPackage> broadcastQueue = new Vector<>();
    private final Vector<DirectedPackage> directedQueue = new Vector<>();
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

    public void sendFrom(BroadcastPackage data) {
      broadcastQueue.add(data);
    }

    public void sendTo(DirectedPackage data) {
      directedQueue.add(data);
    }

    @Override
    public void run() {
      while (!isStopped.get()) {
        sendSelfState();
        sendBroadcast();
        sendDirected();
      }
      Logger.log(this, "run", "ended");
    }

    private void sendSelfState() {
      if (needToSendState.get()) {
        //long t = now();
        try {
          output = isDead.get() ?
                  new DeathPackage(state.generationId).compress() :
                  state.compress();
          LocalPlayer p;
          for (short i = 0; i < players.size; i++) {
            p = players.get(i);
            outputPacket = new DatagramPacket(output, output.length, p.ip, p.port);
            server.send(outputPacket);
          }
          needToSendState.set(false);
        } catch (ArrayIndexOutOfBoundsException | IOException e) {
          e.printStackTrace();
        }
        //System.out.println("send packet took + " + (now() - t) + "ms.");
      }
      if (sequence.size() > 0) {
        try {
          output = sequence.get(0).compress();
          LocalPlayer p;
          for (short i = 0; i < players.size; i++) {
            p = players.get(i);
            outputPacket = new DatagramPacket(output, output.length, p.ip, p.port);
            server.send(outputPacket);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
        sequence.remove(0);
      }
    }

    private void sendBroadcast() {
      if (broadcastQueue.size() > 0) {
        BroadcastPackage data = broadcastQueue.get(0);
        boolean passedException = false;
        try {
          output = data.data;
          LocalPlayer p;
          for (short i = 0; i < players.size; i++) {
            p = players.get(i);
            if (!passedException && p.port == data.port) {
              passedException = true;
              continue;
            }
            outputPacket = new DatagramPacket(output, output.length, p.ip, p.port);
            server.send(outputPacket);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
        broadcastQueue.remove(0);
      }
    }

    private void sendDirected() {
      if (directedQueue.size() > 0) {
        DirectedPackage data = directedQueue.get(0);
        try {
          output = data.data.compress();
          outputPacket = new DatagramPacket(output, output.length, data.ip, data.port);
          server.send(outputPacket);
        } catch (IOException e) {
          e.printStackTrace();
        }
        directedQueue.remove(0);
      }
    }
  }

  private static class BroadcastPackage {
    public final byte[] data;
    public final InetAddress ip;
    public final int port;

    public BroadcastPackage(byte[] data, InetAddress ip, int port) {
      this.data = data;
      this.ip = ip;
      this.port = port;
    }
  }

  private static class DirectedPackage {
    public final DataPackage data;
    public final InetAddress ip;
    public final int port;

    private DirectedPackage(DataPackage data, InetAddress ip, int port) {
      this.data = data;
      this.ip = ip;
      this.port = port;
    }
  }
}
