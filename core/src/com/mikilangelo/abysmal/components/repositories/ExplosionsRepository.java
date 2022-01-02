package com.mikilangelo.abysmal.components.repositories;

import com.mikilangelo.abysmal.models.game.animations.Explosion;
import com.mikilangelo.abysmal.models.game.animations.LaserExplosion;
import com.mikilangelo.abysmal.models.game.animations.explosion.AnimatedExplosion;
import com.mikilangelo.abysmal.ui.screens.GameScreen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

public abstract class ExplosionsRepository {

  private static final Array<LaserExplosion> laserExplosions = new Array<>();
  private static final Array<AnimatedExplosion> animatedExplosions = new Array<>();
  private static final Array<Explosion> explosions = new Array<>();
  private static final Array<Sprite> bigExplosion = new Array<>();
  private static final Array<Sprite> shipExposion = new Array<>();
  private static final Array<Sprite> greenExplosion = new Array<>();
  private static Sprite spark;
  private static Sprite smoke;
  private static Sound sheepExplosionSound;
  private static Sound shieldHit;

  public static void init() {
    sheepExplosionSound = SoundsRepository.getSound("explosions/ship/sound.mp3");
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
      frame.setScale( GameScreen.SCREEN_HEIGHT * 0.2f / frame.getHeight());
      shipExposion.add(frame);
    }
    for (byte i = 0; i < 7; i++) {
      Sprite frame = new Sprite(TexturesRepository.get("explosions/green/FX_6_" + i + ".png"));
      frame.setScale( GameScreen.SCREEN_HEIGHT * 0.35f / frame.getHeight());
      greenExplosion.add(frame);
    }
  }

  public static void addShipExplosion(float x, float y, float soundScale, float pan) {
    sheepExplosionSound.play(soundScale, 1, pan);
    explosions.add(new Explosion(shipExposion, x, y, 0.04f));
    animatedExplosions.add(new AnimatedExplosion(spark, smoke, 8f, x, y));
    AsteroidsRepository.applyImpulse(x, y);
    GameScreen.shakeCamera(2.5f * soundScale);
  }

  public static void shieldHid(float distance) {
    if (distance < 100) {
      shieldHit.play(0.5f - distance * 0.005f);
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
    laserExplosions.clear();
    explosions.clear();
    bigExplosion.clear();
  }
}
