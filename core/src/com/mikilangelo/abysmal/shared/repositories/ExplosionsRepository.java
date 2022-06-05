package com.mikilangelo.abysmal.shared.repositories;

import com.badlogic.gdx.graphics.GL20;
import com.mikilangelo.abysmal.screens.game.actors.decor.animations.Explosion;
import com.mikilangelo.abysmal.screens.game.actors.decor.animations.LaserExplosion;
import com.mikilangelo.abysmal.screens.game.actors.decor.animations.explosion.AnimatedExplosion;
import com.mikilangelo.abysmal.screens.game.GameScreen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

public abstract class ExplosionsRepository {

  private static final Array<LaserExplosion> laserExplosions = new Array<>();
  private static final Array<AnimatedExplosion> animatedExplosions = new Array<>();
  private static final Array<Explosion> explosions = new Array<>();
  private static final Array<Sprite> bigExplosion = new Array<>();
  private static final Array<Sprite> shipExplosion = new Array<>();
  private static final Array<Sprite> greenExplosion = new Array<>();
  private static Sprite spark;
  private static Sprite smoke;
  private static Sound sheepExplosionSound;
  private static Sound shieldHit;

  public static void init() {
    sheepExplosionSound = SoundsRepository.getSound("explosions/simple/sound.mp3");
    shieldHit = SoundsRepository.getSound("sounds/shieldHit.wav");
    spark = new Sprite(TexturesRepository.get("explosions/animated/spark.png"));
    smoke = new Sprite(TexturesRepository.get("explosions/animated/smoke.png"));
    for (byte i = 0; i < 27; i++) {
      Sprite frame = new Sprite(TexturesRepository.get("explosions/big/" + i + ".png"));
      frame.setScale( GameScreen.SCREEN_HEIGHT * 0.45f / frame.getHeight());
      bigExplosion.add(frame);
    }
    for (byte i = 1; i < 7; i++) {
      Sprite frame = new Sprite(TexturesRepository.get("explosions/simple/" + i + ".png"));
      frame.setScale( GameScreen.SCREEN_HEIGHT * 0.21f / frame.getHeight());
      shipExplosion.add(frame);
    }
    for (byte i = 0; i < 7; i++) {
      Sprite frame = new Sprite(TexturesRepository.get("explosions/green/FX_6_" + i + ".png"));
      frame.setScale( GameScreen.SCREEN_HEIGHT * 0.35f / frame.getHeight());
      greenExplosion.add(frame);
    }
  }

  public static void addShipExplosion(float x, float y, float soundScale, float pan) {
    sheepExplosionSound.play(soundScale, 1, pan);
    explosions.add(new Explosion(shipExplosion, x, y, 0.07f));
    animatedExplosions.add(new AnimatedExplosion(spark, smoke, 8.1f, x, y));
    AsteroidsRepository.applyImpulse(x, y);
    GameScreen.shakeCamera(5f * soundScale);
  }

  public static void shieldHid(float distance, float power) {
    if (distance < 100) {
      final float soundPower = (0.75f - distance * 0.0075f) * power;
      if (soundPower > 0.03f) {
        shieldHit.play();
      }
    }
  }

  public static void addExplosion(Explosion e) {
    explosions.add(e);
  }

  public static void addLaserExplosion(LaserExplosion e) {
    laserExplosions.add(e);
  }

  public static void drawLaserExplosions(Batch batch, float delta) {
    for (LaserExplosion e : laserExplosions) {
      e.draw(batch, delta);
    }
    for (int i = 0; i < laserExplosions.size; i++) {
      if (laserExplosions.get(i).ended) {
        laserExplosions.removeIndex(i--);
      }
    }
  }

  public static void drawSimpleExplosions(Batch batch, float delta) {
    for (int i = 0; i < animatedExplosions.size; i++) {
      if (!animatedExplosions.get(i).draw(batch)) {
        animatedExplosions.removeIndex(i--);
      }
    }
    for (Explosion e : explosions) {
      e.draw(batch, delta);
    }
    for (int i = 0; i < explosions.size; i++) {
      if (explosions.get(i).ended) {
        explosions.removeIndex(i--);
      }
    }
  }

  public static void clear() {
    greenExplosion.clear();
    shipExplosion.clear();
    bigExplosion.clear();

    animatedExplosions.clear();
    explosions.clear();
    laserExplosions.clear();
  }
}
