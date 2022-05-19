package com.mikilangelo.abysmal.shared.repositories;

import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_HEIGHT;
import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_WIDTH;

import com.mikilangelo.abysmal.screens.game.actors.fixtures.Asteroid;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.AsteroidCrashed;
import com.mikilangelo.abysmal.screens.game.uiElements.Radar;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mikilangelo.abysmal.shared.tools.Random;


public abstract class AsteroidsRepository {

  private static final Array<Asteroid> asteroids = new Array<>();
  private static final Array<Zone> coveredZones = new Array<>();
  private static final Array<Zone> newZones = new Array<>();

  private static final int ZONE_SIZE = 100;
  private static long seed;
  private static float shipX;
  private static float shipY;

  public static void add(Asteroid a) {
    asteroids.add(a);
  }

  // takes 0 - 2 ms
  public static void drawAll(final Batch batch, float x, float y, float delta, float zoom) {
    updateAsteroids(x, y);
    final float dx = SCREEN_WIDTH * zoom + 4;
    final float dy = SCREEN_HEIGHT * zoom + 4;
    for (Asteroid a: asteroids) {
      a.move(delta);
      if ( Math.abs(a.x - shipX) < dx && Math.abs(a.y - shipY) < dy) {
        a.draw(batch);
      }
    }
  }

  public static void drawAtRadar(Batch batch, Radar radar) {
    for (Asteroid a : asteroids) {
      if (a.asteroidTypeId > 4) {
        radar.drawAsteroid(batch, a.x, a.y);
      }
    }
  }

  public static void drawAtRadar(Batch batch, Radar radar, float cameraRotation) {
    for (Asteroid a : asteroids) {
      if (a.asteroidTypeId > 4) {
        radar.drawAsteroid(batch, a.x, a.y, cameraRotation);
      }
    }
  }

  public static void generateAsteroids(long generationSeed, float playerX, float playerY) {
    seed = generationSeed;
    Zone z = new Zone(Math.round(playerX / ZONE_SIZE), Math.round(playerY / ZONE_SIZE));
    generateAsteroidsIn(z);
    coveredZones.add(z);
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

  private static void updateAsteroids(float x, float y) {
    shipX = x;
    shipY = y;
    // remove destroyed asteroids
    for (int i = 0; i < asteroids.size; i++) {
      if (asteroids.get(i).destroyed) {
        asteroids.get(i).destroyBody();
        asteroids.removeIndex(i--);
      }
    }
    boolean coveredTop = false;
    boolean coveredTopLeft = false;
    boolean coveredTopRight = false;
    boolean coveredBottom = false;
    boolean coveredBottomLeft = false;
    boolean coveredBottomRight = false;
    boolean coveredLeft = false;
    boolean coveredRight = false;
    Asteroid a;
    Zone z;
    for (int i = 0; i < coveredZones.size; i++) {
      z = coveredZones.get(i);
      if (!coveredTop) {
        coveredTop = z.covers(shipX, shipY + ZONE_SIZE);
        if (coveredTop) continue;
      }
      if (!coveredTopLeft) {
        coveredTopLeft = z.covers(shipX - ZONE_SIZE, shipY + ZONE_SIZE);
        if (coveredTopLeft) continue;
      }
      if (!coveredTopRight) {
        coveredTopRight = z.covers(shipX + ZONE_SIZE, shipY + ZONE_SIZE);
        if (coveredTopRight) continue;
      }
      if (!coveredBottom) {
        coveredBottom = z.covers(shipX, shipY - ZONE_SIZE);
        if (coveredBottom) continue;
      }
      if (!coveredBottomLeft) {
        coveredBottomLeft = z.covers(shipX - ZONE_SIZE, shipY - ZONE_SIZE);
        if (coveredBottomLeft) continue;
      }
      if (!coveredBottomRight) {
        coveredBottomRight = z.covers(shipX + ZONE_SIZE, shipY - ZONE_SIZE);
        if (coveredBottomRight) continue;
      }
      if (!coveredLeft) {
        coveredLeft = z.covers(shipX - ZONE_SIZE, shipY);
        if (coveredLeft) continue;
      }
      if (!coveredRight) {
        coveredRight = z.covers(shipX + ZONE_SIZE, shipY);
        if (coveredRight) continue;
      }
      if ( Math.abs(z.startY + ZONE_SIZE * 0.5f - shipY) > ZONE_SIZE * 2.5f ||
              Math.abs(z.startX + ZONE_SIZE * 0.5f - shipX) > ZONE_SIZE * 2.5f
      ) {
        System.out.println("\nleft zone: [" + z.startX + ", " + z.endX  +" ] * ["
                + z.startY +  "," + + z.endY + "]");
        int counter = 0;
        for (int j = 0; j < asteroids.size; j++) {
          a = asteroids.get(j);
          if (z.covers(a.x, a.y)) {
            a.destroyBody();
            counter++;
            asteroids.removeIndex(j--);
          }
        }
        System.out.println("removed " + counter + " asteroids");
        coveredZones.removeIndex(i--);
      }
    }
    if (!coveredTop) {
      newZones.add(new Zone(Math.round(shipX / ZONE_SIZE),
              Math.round(shipY / ZONE_SIZE) + 1));
    }
    if (!coveredTopLeft) {
      newZones.add(new Zone(Math.round(shipX / ZONE_SIZE) - 1,
              Math.round(shipY / ZONE_SIZE) + 1));
    }
    if (!coveredTopRight) {
      newZones.add(new Zone(Math.round(shipX / ZONE_SIZE) + 1,
              Math.round(shipY / ZONE_SIZE) + 1));
    }
    if (!coveredBottom) {
      newZones.add(new Zone(Math.round(shipX / ZONE_SIZE),
              Math.round(shipY / ZONE_SIZE) - 1));
    }
    if (!coveredBottomLeft) {
      newZones.add(new Zone(Math.round(shipX / ZONE_SIZE) - 1,
              Math.round(shipY / ZONE_SIZE) - 1));
    }
    if (!coveredBottomRight) {
      newZones.add(new Zone(Math.round(shipX / ZONE_SIZE) + 1,
              Math.round(shipY / ZONE_SIZE) - 1));
    }
    if (!coveredLeft) {
      newZones.add(new Zone(Math.round(shipX / ZONE_SIZE) - 1,
              Math.round(shipY / ZONE_SIZE)));
    }
    if (!coveredRight) {
      newZones.add(new Zone(Math.round(shipX / ZONE_SIZE) + 1,
              Math.round(shipY / ZONE_SIZE)));
    }
    if (newZones.size > 0) {
      System.out.println("\nnew zones: " + newZones.size);
      System.out.println(coveredTopLeft + " " + coveredTop + " " + coveredTopRight);
      System.out.println(coveredLeft + " ---- " + coveredRight);
      System.out.println(coveredBottomLeft + " " + coveredBottom + " " + coveredBottomRight);
      System.out.println("total zones: " + (coveredZones.size + newZones.size));
    }
    while (newZones.size > 0) {
      z = newZones.get(0);
      generateAsteroidsIn(z);
      coveredZones.add(z);
      newZones.removeIndex(0);
    }
  }

  private static void generateAsteroidsIn(Zone z) {
    float amount = z.random.nextInt(5, 10) * ZONE_SIZE / 190f;
    for (short i = 0; i < amount; i++) {
      generateAsteroidsGroup(z.randomX(), z.randomY(), z.random);
    }
  }

  private static void generateAsteroidsGroup(float x, float y, Random random) {
    int amount = random.nextInt(1, 7);
    Vector2 position;
    for (byte i = 0; i < amount; i++) {
      position = random.nextGaussian().scl(7f);
      asteroids.add(new Asteroid(
              random.getSeed(),
              random.nextInt(random.nextInt(1, Asteroid.smallAmount), Asteroid.typesAmount) - 1,
              x + position.x,
              y + position.y
      ));
    }
  }

  public static void handleCrash(AsteroidCrashed crashData) {
    Asteroid a;
    for (int i = 0; i < asteroids.size; i++) {
      a = asteroids.get(i);
      if (a.asteroidId == crashData.asteroidId) {
        a.setExploded(crashData.x, crashData.y, crashData.angle);
        return;
      }
    }
  }

  public static void clear() {
    for (Asteroid a : asteroids) {
      a.destroyBody();
    }
    asteroids.clear();
    newZones.clear();
    coveredZones.clear();
  }

  private static class Zone {
    public final int startX, endX;
    public final int startY, endY;
    public final Random random;

    public Zone(int zoneXId, int zoneYId) {
      startX = ZONE_SIZE * zoneXId - ZONE_SIZE / 2;
      endX = startX + ZONE_SIZE;
      startY = ZONE_SIZE * zoneYId - ZONE_SIZE / 2;
      endY = startY + ZONE_SIZE;
      this.random = new Random(seed + (long) zoneXId * zoneYId - zoneXId % 7 + zoneYId % 3);
    }

    public boolean covers(float x, float y) {
      return x > startX && x <= endX && y > startY && y <= endY;
    }

    public float randomX() {
      return random.nextFloat(startX, endX);
    }

    public float randomY() {
      return random.nextFloat(startY, endY);
    }

  }

}
