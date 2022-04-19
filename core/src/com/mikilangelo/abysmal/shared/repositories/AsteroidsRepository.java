package com.mikilangelo.abysmal.shared.repositories;

import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_HEIGHT;
import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_WIDTH;

import com.mikilangelo.abysmal.screens.game.actors.fixtures.Asteroid;
import com.mikilangelo.abysmal.screens.game.uiElements.Radar;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;


public abstract class AsteroidsRepository {

  private static final Array<Asteroid> asteroids = new Array<>();
  private static final Array<Zone> coveredZones = new Array<>();
  private static final Array<Zone> newZones = new Array<>();

  private static final int ZONE_SIZE = 150;
  private static long seed;
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

  private static void updateAsteroids(Vector2 shipPos) {
    shipX = shipPos.x;
    shipY = shipPos.y;
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
        continue;
      }
      if (!coveredTopLeft) {
        coveredTopLeft = z.covers(shipX - ZONE_SIZE, shipY + ZONE_SIZE);
        continue;
      }
      if (!coveredTopRight) {
        coveredTopRight = z.covers(shipX + ZONE_SIZE, shipY + ZONE_SIZE);
        continue;
      }
      if (!coveredBottom) {
        coveredBottom = z.covers(shipX, shipY - ZONE_SIZE);
        continue;
      }
      if (!coveredBottomLeft) {
        coveredBottomLeft = z.covers(shipX - ZONE_SIZE, shipY - ZONE_SIZE);
        continue;
      }
      if (!coveredBottomRight) {
        coveredBottomRight = z.covers(shipX + ZONE_SIZE, shipY - ZONE_SIZE);
        continue;
      }
      if (!coveredLeft) {
        coveredLeft = z.covers(shipX - ZONE_SIZE, shipY);
        continue;
      }
      if (!coveredRight) {
        coveredRight = z.covers(shipX + ZONE_SIZE, shipY);
        continue;
      }
      if ( Math.abs(z.startY + ZONE_SIZE - shipY) > ZONE_SIZE * 3 ||
              Math.abs(z.startX + ZONE_SIZE - shipX) > ZONE_SIZE * 3
      ) {
        for (int j = 0; j < asteroids.size; j++) {
          a = asteroids.get(j);
          if (z.covers(a.x, a.y)) {
            a.destroyBody();
            asteroids.removeIndex(j--);
          }
        }
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
      System.out.println("new zones: " + newZones.size);
    }
    while (newZones.size > 0) {
      z = newZones.get(0);
      generateAsteroidsIn(z);
      coveredZones.add(z);
      newZones.removeIndex(0);
    }
  }

  private static void generateAsteroidsIn(Zone z) {
    int amount = z.random.nextInt(5, 10);
    for (short i = 0; i < amount; i++) {
      generateAsteroidsGroup(z.randomX(), z.randomY(), z.random);
    }
  }

  private static void generateAsteroidsGroup(float x, float y, Random random) {
    int amount = random.nextInt(1, 7);
    for (byte i = 0; i < amount; i++) {
      asteroids.add(new Asteroid(
              random.nextInt(random.nextInt(1, Asteroid.smallAmount), Asteroid.typesAmount) - 1,
              x + random.nextFloat(-13.3f, 13.3f),
              y + random.nextFloat(-13.3f, 13.3f)
      ));
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

  private static class Random {

    private long seed;

    public Random(long seed) {
      this.seed = seed;
    }

    public int nextInt(int start, int end) {
      seed = (seed * 73129 + 12345) % 1000000;
      return start + ((int) (seed / 7) % (end - start + 1));
    }

    /**
     * @return pseudo-random float from [0, 1]
     */
    public float nextFloat() {
      seed = (seed * 73129 + 12345) % 1000000;
      return ((int) (seed / 7) % 32768) / 32767f;
    }

    /**
     * @param start - start of the range
     * @param end - end of the range
     * @return pseudo-random float from [start, end]
     */
    public float nextFloat(float start, float end) {
      return start + nextFloat() * (end - start);
    }
  }

}
