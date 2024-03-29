package com.mikilangelo.abysmal.screens.game.enemies.bots;

import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_HEIGHT;
import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_WIDTH;
import static com.mikilangelo.abysmal.screens.game.GameScreen.camera;
import static com.mikilangelo.abysmal.screens.game.GameScreen.world;

import com.mikilangelo.abysmal.screens.game.enemies.EnemiesProcessor;
import com.mikilangelo.abysmal.shared.Settings;
import com.mikilangelo.abysmal.shared.ShipDefinitions;
import com.mikilangelo.abysmal.screens.game.actors.ship.Ship;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.ShotData;
import com.mikilangelo.abysmal.shared.tools.CalculateUtils;
import com.mikilangelo.abysmal.screens.game.uiElements.Radar;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.mikilangelo.abysmal.shared.tools.Logger;

public class BotsProcessor implements EnemiesProcessor {

  public final Array<Bot> bots = new Array<>();
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
    startRangeX = playerX - SCREEN_WIDTH * 2;
    endRangeX = playerX + SCREEN_WIDTH * 2;
    startRangeY = playerY - SCREEN_HEIGHT * 2;
    endRangeY = playerY + SCREEN_HEIGHT * 2;
    if (Settings.debug) {
      final Ship bot = new Ship(
              ShipDefinitions.get("invader"),
              playerX + MathUtils.random(-19.3f, 19.3f),
              playerY + MathUtils.random(-19.3f, 19.3f), false,
              playerX, playerY);
      bot.angle = bot.newAngle = MathUtils.random(6.28f);
      newShips.add(bot);
    }
  }

  private void updateBots() {
    int botsAMount = this.bots.size;
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
    if (botsAMount != this.bots.size) {
      Logger.log(this, "updateBots", bots.size - botsAMount + " bots added. total amount is " + bots.size);
    }
  }

  private void generateBotsIn(float xMin, float xMax, float yMin, float yMax) { // 650
    float normalSquare = (xMax - xMin) * (yMax - yMin) / SCREEN_HEIGHT / SCREEN_WIDTH / 650f;
    int amount = Math.round(MathUtils.random(6f, 14f) * normalSquare);
    for (short i = 0; i < amount; i++) {
      generateBotsGroup(MathUtils.random(xMin, xMax), MathUtils.random(yMin, yMax));
    }
    Logger.log(this, "generateBotsIn", "created " + amount + "groups.");
  }

  private void generateBotsGroup(float x, float y) {
    int amount = MathUtils.random(1, MathUtils.random(2, 4));
    for (byte i = 0; i < amount; i++) {
      String def = CalculateUtils.testProbability(0.05f) ? "alien" :
              CalculateUtils.testProbability(0.1f) ? "hyperion" :
              CalculateUtils.testProbability(0.34f) ? "invader" : "defender";
      final Ship ship = new Ship(
              ShipDefinitions.get(def),
              x + MathUtils.random(-25f, 25f),
              y + MathUtils.random(-25f, 25f), false,
              playerX, playerY);
      ship.angle = ship.newAngle = MathUtils.random(6.28f);
      newShips.add(ship);
    }
    Logger.log(this, "generateBotsGroup", "created " + amount + "bots.");
  }

  @Override
  public void process(Ship player, final float delta) {
    if (!botsProcessed) {
      Logger.log(this, "process", "missed frame");
      return;
    }
    if (player.bodyData.health <= 0) {
      return;
    }
    playerX = player.x;
    playerY = player.y;
    updateBots();
    for (int i = 0; i < bots.size; i++) {
      if ( !bots.get(i).control(playerX, playerY, player.angle, delta) ) {
        Logger.log(this, "process", "Removed ship: " + bots.get(i).ship.generationId);
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
      if (Settings.debug) {
        batch.end();
        for (Bot bot: bots) {
          bot.drawDirection(camera);
        }
        batch.begin();
      }
    } catch (NullPointerException e) {
      Logger.log(this, "drawAll", e.getMessage());
    }
  }

  @Override
  public void drawAtRadar(Batch batch, Radar radar) {
    for (Bot b : bots) {
      radar.drawEnemy(batch, b.ship.x, b.ship.y);
    }
  }

  @Override
  public void drawAtRadar(Batch batch, Radar radar, float cameraRotation) {
    for (Bot b : bots) {
      radar.drawEnemy(batch, b.ship.x, b.ship.y, cameraRotation);
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

  @Override
  public void explodeAsteroid(long asteroidId, float x, float y, float angle) { }

}
