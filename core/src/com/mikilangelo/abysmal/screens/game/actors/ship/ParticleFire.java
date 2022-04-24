package com.mikilangelo.abysmal.screens.game.actors.ship;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mikilangelo.abysmal.shared.defenitions.EngineDef;
import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;

public class ParticleFire extends Particle {

  private static final EngineDef definition = new EngineDef();
  static {
    definition.particleTexture = new Sprite(TexturesRepository.get("explosions/animated/spark.png"));
    definition.color[0] = 0.95f;
    definition.color[1] = 0.3f;
    definition.color[2] = 0.01f;
    definition.withTint = true;
    definition.particlePositionDispersion = 0.45f;
    definition.particleSpeedDispersion = 1.9f;
    definition.decayRate = 0.007f / 0.17f;
    definition.particleScale = 0.016f;
    definition.particleSizeDispersion = 0.007f;
    definition.particleShipSpeedCoefficient = 0.84f;
    definition.initialParticleOpacity = 1f;
    definition.lightDecay = 0.037f;
  }

  public ParticleFire(float x, float y, float speedX, float speedY) {
    super(definition, x, y, speedX, speedY);
  }
}
