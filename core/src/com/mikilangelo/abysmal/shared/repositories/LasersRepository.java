package com.mikilangelo.abysmal.shared.repositories;

import com.mikilangelo.abysmal.screens.game.actors.ship.Laser;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;

public abstract class LasersRepository {

  private static final Array<Laser> lasers = new Array<>();
  private static final Array<Laser> turretLasers = new Array<>();

  public static void addSimple(Laser l) {
    lasers.add(l);
  }

  public static void addTurret(Laser l) {
    turretLasers.add(l);
  }

  public static void drawSimple(Batch batch, float delta) {
    for (int i = 0; i < lasers.size; i++) {
      final Laser l = lasers.get(i);
      if (l.ended) {
        lasers.removeIndex(i--);
      } else {
        l.move(delta);
        l.draw(batch);
      }
    }
  }

  public static void drawTurrets(Batch batch, float delta) {
    for (int i = 0; i < turretLasers.size; i++) {
      final Laser l = turretLasers.get(i);
      if (l.ended) {
        turretLasers.removeIndex(i--);
      } else {
        l.move(delta);
        l.draw(batch);
      }
    }
  }

  public static void clear() {
    for (Laser l : lasers) {
      l.destroyBody();
    }
    lasers.clear();
    for (Laser l : turretLasers) {
      l.destroyBody();
    }
    turretLasers.clear();
  }
}
