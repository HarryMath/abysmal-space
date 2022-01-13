package com.mikilangelo.abysmal.components.repositories;

import com.mikilangelo.abysmal.models.game.extended.EngineParticle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;

public abstract class ParticlesRepository {

  private static final Array<EngineParticle> particles = new Array<>();
  private static final Array<EngineParticle> particlesTop = new Array<>();
  private static EngineParticle p;

  public static void add(EngineParticle particle, boolean isTopLayer) {
    if (particles.size > 1700) {
      particles.removeIndex(0);
      particles.removeIndex(0);
    }
    if (isTopLayer) {
      particlesTop.add(particle);
    } else {
      particles.add(particle);
    }
  }

  public static void drawAll(Batch batch, float delta) {
    for (int i = 0; i < particles.size; i++) {
      p = particles.get(i);
      if (p.opacity <= 0) {
        particles.removeIndex(i--);
      } else {
        p.move(delta);
        p.draw(batch);
      }
    }
    for (int i = 0; i < particlesTop.size; i++) {
      p = particlesTop.get(i);
      if (p.opacity <= 0) {
        particlesTop.removeIndex(i--);
      } else {
        p.move(delta);
        p.draw(batch);
      }
    }
  }

  public static void clear() {
    particles.clear();
    particlesTop.clear();
  }
}
