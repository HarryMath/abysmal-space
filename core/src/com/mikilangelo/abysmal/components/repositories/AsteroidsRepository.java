package com.mikilangelo.abysmal.components.repositories;

import static com.mikilangelo.abysmal.ui.screens.GameScreen.SCREEN_HEIGHT;
import static com.mikilangelo.abysmal.ui.screens.GameScreen.SCREEN_WIDTH;

import com.mikilangelo.abysmal.models.game.extended.Asteroid;
import com.mikilangelo.abysmal.ui.gameElemets.Radar;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;


public abstract class AsteroidsRepository {

  private static final Array<Asteroid> asteroids = new Array<>();
  private static float startRangeX;
  private static float endRangeX;
  private static float startRangeY;
  private static float endRangeY;
  private static float shipX;
  private static float shipY;

  public static void add(Asteroid a) {
    asteroids.add(a);
  }

  public static void drawAll(final Batch batch, Vector2 shipPos, final float delta) {
    updateAsteroids(shipPos);
    asteroids.forEach(a -> {
      a.move(delta);
      a.draw(batch);
    });
  }
  public static void drawAtRadar(Batch batch, Radar radar) {
    for (Asteroid a : asteroids) {
      if (a.asteroidTypeId > 4) {
        radar.drawAsteroid(batch, a.x, a.y);
      }
    }
  }

  public static void generateAsteroids(float playerX, float playerY) {
    startRangeX = playerX - SCREEN_WIDTH * 4;
    endRangeX = playerX + SCREEN_WIDTH * 4;
    startRangeY = playerY - SCREEN_HEIGHT * 4;
    endRangeY = playerY + SCREEN_HEIGHT * 4;
    generateAsteroidsIn(startRangeX, endRangeX, startRangeY, endRangeY);
  }

  public static void applyImpulse(float x, float y) {
    float power, dx, dy, ax, ay;
    for (Asteroid a: asteroids) {
      ax = a.body.getWorldCenter().x;
      ay = a.body.getWorldCenter().y;
      dx = Math.abs(ax - x);
      dy = Math.abs(ay - y);
      if ( !a.destroyed &&
              Math.abs(ax - shipX) < SCREEN_WIDTH * 3 &&
              Math.abs(ay - shipY) < SCREEN_HEIGHT * 3 &&
              dx > 0.2f && dy > 0.2f
      ) {
        power = 800 / (dx * dx + dy * dy) * a.getSqrtMass();
        a.body.applyLinearImpulse(
                power * (ax - x) / dx,
                power * (ay - y) / dy,
                ax, ay, true
        );
      }
    }
  }

  private static void updateAsteroids(Vector2 shipPos) {
    shipX = shipPos.x;
    shipY = shipPos.y;
    for (int i = 0; i < asteroids.size; i++) {
      if ( asteroids.get(i).destroyed ||
              Math.abs(asteroids.get(i).x - shipX) > SCREEN_WIDTH * 4 ||
              Math.abs(asteroids.get(i).y - shipY) > SCREEN_HEIGHT * 4
      ) {
        asteroids.get(i).destroyBody();
        asteroids.removeIndex(i--);
      }
    }
    startRangeX = Math.max(startRangeX, shipX - SCREEN_WIDTH * 4);
    endRangeX = Math.min(endRangeX, shipX + SCREEN_WIDTH * 4);
    startRangeY = Math.max(startRangeY, shipY - SCREEN_HEIGHT * 4);
    endRangeY = Math.min(endRangeY, shipY + SCREEN_HEIGHT * 4);
    if (startRangeX > shipX - SCREEN_WIDTH * 2f) {
      generateAsteroidsIn(shipX - SCREEN_WIDTH * 4, startRangeX, startRangeY, endRangeY);
      startRangeX = shipX - SCREEN_WIDTH * 4;
    } else if (endRangeX < shipX + SCREEN_WIDTH * 2f){
      generateAsteroidsIn(endRangeX, shipX + SCREEN_WIDTH * 4, startRangeY, endRangeY);
      endRangeX = shipX + SCREEN_WIDTH * 4;
    }
    if (startRangeY > shipY - SCREEN_HEIGHT * 2) {
      generateAsteroidsIn(startRangeX, endRangeX, shipY - SCREEN_HEIGHT * 4, startRangeY);
      startRangeY = shipY - SCREEN_HEIGHT * 4;
    } else if (endRangeY < shipY + SCREEN_HEIGHT * 2){
      generateAsteroidsIn(startRangeX, endRangeX, endRangeY, shipY + SCREEN_HEIGHT * 4);
      endRangeY = shipY + SCREEN_HEIGHT * 4;
    }
  }

  private static void generateAsteroidsIn(float xMin, float xMax, float yMin, float yMax) {
    float normalSquare = (xMax - xMin) * (yMax - yMin) / SCREEN_HEIGHT / SCREEN_WIDTH / 100f;
    int amount = Math.round(MathUtils.random(11f, 37f) * normalSquare);
    for (short i = 0; i < amount; i++) {
      generateAsteroidsGroup(MathUtils.random(xMin, xMax), MathUtils.random(yMin, yMax));
    }
  }

  private static void generateAsteroidsGroup(float x, float y) {
    int amount = MathUtils.random(1, 7);
    for (byte i = 0; i < amount; i++) {
      asteroids.add(new Asteroid(
              MathUtils.random(MathUtils.random(1, Asteroid.smallAmount), Asteroid.typesAmount) - 1,
              x + MathUtils.random(-13.3f, 13.3f),
              y + MathUtils.random(-13.3f, 13.3f)
      ));
    }
  }

  public static void clear() {
    for (Asteroid a : asteroids) {
      a.destroyBody();
    }
    asteroids.clear();
  }

}
