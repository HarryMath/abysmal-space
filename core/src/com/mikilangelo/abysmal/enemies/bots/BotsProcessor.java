package com.mikilangelo.abysmal.enemies.bots;

import static com.mikilangelo.abysmal.ui.screens.GameScreen.SCREEN_HEIGHT;
import static com.mikilangelo.abysmal.ui.screens.GameScreen.SCREEN_WIDTH;
import static com.mikilangelo.abysmal.ui.screens.GameScreen.camera;
import static com.mikilangelo.abysmal.ui.screens.GameScreen.world;

import com.mikilangelo.abysmal.components.Settings;
import com.mikilangelo.abysmal.components.ShipDefinitions;
import com.mikilangelo.abysmal.enemies.EnemiesProcessor;
import com.mikilangelo.abysmal.models.game.Ship;
import com.mikilangelo.abysmal.models.sending.ShotData;
import com.mikilangelo.abysmal.tools.Geometry;
import com.mikilangelo.abysmal.ui.gameElemets.Radar;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class BotsProcessor implements EnemiesProcessor {

  private final Array<Bot> bots = new Array<>();
  private float startRangeX;
  private float endRangeX;
  private float startRangeY;
  private float endRangeY;

  private float playerX, playerY;
  boolean botsProcessed = true;
  boolean threadWorking = true;
  final Array<Ship> newShips = new Array<>();

  @Override
  public Ship getNearestEnemy(float playerX, float playerY) {
    float minDistance = endRangeX * 10;
    Ship nearestShip = null;
    for (Bot b: bots) {
      if (b.ship.distance < minDistance) {
        minDistance = b.ship.distance;
        nearestShip = b.ship;
      }
    }
    return nearestShip;
  }

  @Override
  public void generateEnemies(Ship ship) {
    playerX = ship.x; playerY = ship.y;
    if (Settings.debug) {
      final Ship bot = new Ship(
              ShipDefinitions.get("hyperion"),
              playerX + MathUtils.random(-19.3f, 19.3f),
              playerY + MathUtils.random(-19.3f, 19.3f), false,
              playerX, playerY);
      bot.angle = bot.newAngle = MathUtils.random(6.28f);
      newShips.add(bot);
    }
    final Thread botsThread = new Thread(() -> {
      startRangeX = playerX - SCREEN_WIDTH * 8;
      endRangeX = playerX + SCREEN_WIDTH * 8;
      startRangeY = playerY - SCREEN_HEIGHT * 8;
      endRangeY = playerY + SCREEN_HEIGHT * 8;
      generateBotsIn(startRangeX, (startRangeX + playerX) / 2, startRangeY, endRangeY);
      generateBotsIn(endRangeX, (endRangeX + playerX) / 2, startRangeY, endRangeY);
      generateBotsIn((startRangeX + playerX) / 2, (endRangeX + playerX) / 2, startRangeY, (startRangeY + playerY) / 2);
      generateBotsIn((startRangeX + playerX) / 2, (endRangeX + playerX) / 2, endRangeY, (endRangeY + playerY) / 2);
      while (threadWorking) {
        botsProcessed = false;
        updateBots();
        botsProcessed = true;
        try {
          Thread.sleep(50);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
    //botsThread.setPriority(Thread.MIN_PRIORITY);
    //botsThread.start();
  }

  private void updateBots() {
    startRangeX = Math.max(startRangeX, playerX - SCREEN_WIDTH * 8);
    endRangeX = Math.min(endRangeX, playerX + SCREEN_WIDTH * 8);
    startRangeY = Math.max(startRangeY, playerY - SCREEN_HEIGHT * 8);
    endRangeY = Math.min(endRangeY, playerY + SCREEN_HEIGHT * 8);
    if (startRangeX > playerX - SCREEN_WIDTH * 4f) {
      generateBotsIn(playerX - SCREEN_WIDTH * 8, startRangeX, startRangeY, endRangeY);
      startRangeX = playerX - SCREEN_WIDTH * 8;
    } else if (endRangeX < playerX + SCREEN_WIDTH * 4f){
      generateBotsIn(endRangeX, playerX + SCREEN_WIDTH * 8, startRangeY, endRangeY);
      endRangeX = playerX + SCREEN_WIDTH * 8;
    }
    if (startRangeY > playerY - SCREEN_HEIGHT * 4) {
      generateBotsIn(startRangeX, endRangeX, playerY - SCREEN_HEIGHT * 8, startRangeY);
      startRangeY = playerY - SCREEN_HEIGHT * 8;
    } else if (endRangeY < playerY + SCREEN_HEIGHT * 4){
      generateBotsIn(startRangeX, endRangeX, endRangeY, playerY + SCREEN_HEIGHT * 8);
      endRangeY = playerY + SCREEN_HEIGHT * 8;
    }
  }

  private void generateBotsIn(float xMin, float xMax, float yMin, float yMax) { // 650
    float normalSquare = (xMax - xMin) * (yMax - yMin) / SCREEN_HEIGHT / SCREEN_WIDTH / 600f;
    int amount = Math.round(MathUtils.random(7f, 17f) * normalSquare);
    for (short i = 0; i < amount; i++) {
      generateBotsGroup(MathUtils.random(xMin, xMax), MathUtils.random(yMin, yMax));
    }
  }

  private void generateBotsGroup(float x, float y) {
    int amount = MathUtils.random(1, 2);
    for (byte i = 0; i < amount; i++) {
//      int def = MathUtils.random(0, ShipDefinitions.shipDefinitions.size);
      String def = Geometry.getProbability(0.05f) ? "alien" :
              Geometry.getProbability(0.1f) ? "hyperion" :
              Geometry.getProbability(0.34f) ? "invader" : "defender";
      final Ship ship = new Ship(
              ShipDefinitions.get(def),
              x + MathUtils.random(-19.3f, 19.3f),
              y + MathUtils.random(-19.3f, 19.3f), false,
              playerX, playerY);
      ship.angle = ship.newAngle = MathUtils.random(6.28f);
      newShips.add(ship);
    }
  }

  @Override
  public void process(Ship player, final float delta) {
    if (!botsProcessed) {
      System.out.println("missed");
      return;
    }
    playerX = player.x;
    playerY = player.y;
    updateBots();
    for (int i = 0; i < bots.size; i++) {
      if ( !bots.get(i).control(playerX, playerY, player.angle, delta) ) {
        System.out.println("REMOVED SHIP: " + bots.get(i).ship.generationId + "\n");
        bots.get(i).ship.destroy(world);
        bots.removeIndex(i--);
      }
    }
    for (Ship ship : newShips) {
      ship.createBody(world);
      bots.add(new Bot(ship));
    }
    newShips.clear();
  }

  @Override
  public void drawAll(Batch batch, float delta) {
    try {
      final float w = SCREEN_WIDTH * camera.zoom;
      final float h = SCREEN_HEIGHT * camera.zoom;
      final float x = camera.X;
      final float y = camera.Y;
      for (Bot bot: bots) {
        bot.draw(batch, delta, x, y, w, h);
      }
    } catch (NullPointerException ignore) {}
  }

  @Override
  public void drawAtRadar(Batch batch, Radar radar) {
    for (Bot b : bots) {
      radar.drawEnemy(batch, b.ship.x, b.ship.y);
    }
  }

  @Override
  public void dispose() {
    threadWorking = false;
    for (Bot bot: bots) {
      bot.ship.destroy(world);
    }
    this.bots.clear();
  }

  @Override
  public void shot(ShotData shotData) { }

}
