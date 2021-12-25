package com.mikilangelo.abysmal.enemies.bots;

import static com.mikilangelo.abysmal.ui.screens.GameScreen.SCREEN_HEIGHT;
import static com.mikilangelo.abysmal.ui.screens.GameScreen.SCREEN_WIDTH;
import static com.mikilangelo.abysmal.ui.screens.GameScreen.world;

import com.mikilangelo.abysmal.components.ShipDefinitions;
import com.mikilangelo.abysmal.components.repositories.ExplosionsRepository;
import com.mikilangelo.abysmal.enemies.EnemiesProcessor;
import com.mikilangelo.abysmal.models.game.Ship;
import com.mikilangelo.abysmal.models.game.extended.Turret;
import com.mikilangelo.abysmal.models.objectsData.ShipData;
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
    final Thread botsThread = new Thread(new Runnable() {
      @Override
      public void run() {
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
      }
    });
    botsThread.setPriority(Thread.MIN_PRIORITY);
    // botsThread.start();
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
    float normalSquare = (xMax - xMin) * (yMax - yMin) / SCREEN_HEIGHT / SCREEN_WIDTH / 550f;
    int amount = Math.round(MathUtils.random(7f, 17f) * normalSquare);
    for (short i = 0; i < amount; i++) {
      generateBotsGroup(MathUtils.random(xMin, xMax), MathUtils.random(yMin, yMax));
    }
  }

  private void generateBotsGroup(float x, float y) {
    int amount = MathUtils.random(1, 2);
    for (byte i = 0; i < amount; i++) {
//      int def = MathUtils.random(0, ShipDefinitions.shipDefinitions.size);
      String def = MathUtils.random(0, 21) % 11 == 0 ? "hyperion" :
              MathUtils.random(1, 11) % 4 == 0 ? "invader" : "defender";
      final Ship ship = new Ship(
              ShipDefinitions.get(def),
              x + MathUtils.random(-19.3f, 19.3f),
              y + MathUtils.random(-19.3f, 19.3f), false,
              playerX, playerY);
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
      if ( !bots.get(i).control(playerX, playerY, delta) ) {
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
      for (Bot bot: bots) {
        if (bot.ship.distance < SCREEN_WIDTH * 3) {
          bot.ship.draw(batch, delta);
        }
      }
    } catch (NullPointerException ignore) {}
  }

  @Override
  public void drawAtRadar(Batch batch, Radar radar, float playerX, float playerY) {
    for (Bot b : bots) {
      radar.drawEnemy(batch, playerX, playerY, b.ship.x, b.ship.y);
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
  public void shot() { }

  private static class Bot {
    private float biasAngle;
    private final float biasAngleSpeed;
    private final float biasRadius;
    // private float distance;
    Ship ship;

    // preControl values
    float aimX, aimY;
    float aimAngle = 0;

    public Bot(Ship ship) {
      this.biasAngle = MathUtils.random(0, MathUtils.PI2);
      this.biasRadius = ship.turrets.size > 0 ?
              MathUtils.random(7f, 17f) : MathUtils.random(4f, 14f);
      final float aimSpeed = MathUtils.random(MathUtils.PI/50, MathUtils.PI/10) / biasRadius;
      this.biasAngleSpeed = biasAngle > MathUtils.PI ? aimSpeed : -aimSpeed;
      this.ship = ship;
    }

    public boolean control(float playerX, float playerY, float delta) {
      ship.distance = Geometry.distance(playerX, playerY, ship.x, ship.y);
      if (ship.distance > SCREEN_WIDTH * 9) {
        return false;
      }
      if (((ShipData) ship.body.getUserData()).health <= 0) {
        if (ship.distance < 100) {
          ExplosionsRepository.addShipExplosion(ship.x, ship.y,
                  1 - ship.distance * 0.01f, (ship.x - playerX) / ship.distance);
        }
        return false;
      }
      if (ship.distance < ship.definition.radarPower) {
        ship.move(delta);
        if (ship.distance > 4) {
          aimAngle = Geometry.defineAngle(playerX - ship.x, playerY - ship.y, aimAngle);
          ship.control( aimAngle, ship.distance > 20 ? 1 : ship.distance / 20, delta);
          final float radius  = biasRadius + ship.distance / SCREEN_WIDTH;
          biasAngle += biasAngleSpeed;
          aimX = (playerX + radius * MathUtils.cos(aimAngle)) * 20;
          aimY = (playerY + radius * MathUtils.sin(aimAngle)) * 20;
          if (ship.distance < SCREEN_WIDTH * 0.4f * ship.definition.maxZoom ) {
            boolean needToShotTurrets = false;
            for (Turret t: ship.turrets) {
              t.control(aimAngle);
              if (Math.abs((t.angle + ship.angle) % MathUtils.PI2 - t.newAngle) < 0.011f ) {
                needToShotTurrets = true;
                break;
              }
            }
            if (needToShotTurrets || Math.abs(aimAngle - ship.angle) < 0.01f) {
              ship.shot();
            }
          }
        }
      }
      return true;
    }
  }
}
