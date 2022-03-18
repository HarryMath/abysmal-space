package com.mikilangelo.abysmal.shared.repositories;

import com.badlogic.gdx.graphics.GL20;
import com.mikilangelo.abysmal.screens.game.actors.ship.EngineParticle;
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
    batch.enableBlending();
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
    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
  }

  public static void clear() {
    particles.clear();
    particlesTop.clear();
  }
}
