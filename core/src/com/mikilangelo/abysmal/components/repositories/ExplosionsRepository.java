package com.mikilangelo.abysmal.components.repositories;

import com.mikilangelo.abysmal.models.game.animations.Explosion;
import com.mikilangelo.abysmal.models.game.animations.LaserExplosion;
import com.mikilangelo.abysmal.ui.screens.GameScreen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

public abstract class ExplosionsRepository {

  private static final Array<LaserExplosion> laserExplosions = new Array<>();
  private static final Array<Explosion> explosions = new Array<>();
  private static final Array<Sprite> shipExplosion = new Array<>();
  private static final Array<Sprite> greenExplosion = new Array<>();
  private static Sound sheepExplosionSound;
  private static Sound shieldHit = SoundsRepository.getSound("sounds/shieldHit.wav");

  public static void init() {
    sheepExplosionSound = SoundsRepository.getSound("explosions/ship/sound.mp3");
    for (byte i = 0; i < 27; i++) {
      Sprite frame = new Sprite(TexturesRepository.get("explosions/big/" + i + ".png"));
      frame.setScale( GameScreen.SCREEN_HEIGHT * 0.45f / frame.getHeight());
      shipExplosion.add(frame);
    }
    for (byte i = 0; i < 7; i++) {
      Sprite frame = new Sprite(TexturesRepository.get("explosions/green/FX_6_" + i + ".png"));
      frame.setScale( GameScreen.SCREEN_HEIGHT * 0.35f / frame.getHeight());
      greenExplosion.add(frame);
    }
  }

  public static void addShipExplosion(float x, float y, float soundScale) {
    sheepExplosionSound.play(soundScale);
    explosions.add(new Explosion(shipExplosion, x, y, 0.035f));
    AsteroidsRepository.applyImpulse(x, y);
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
    shipExplosion.clear();
  }
}
