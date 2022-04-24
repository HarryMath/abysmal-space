package com.mikilangelo.abysmal.screens.game.actors.ship;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mikilangelo.abysmal.shared.defenitions.EngineDef;
import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;

public class ParticleSmog extends Particle {

  private static final EngineDef definition = new EngineDef();
  static {
    definition.particleTexture = new Sprite(TexturesRepository.get("explosions/animated/smoke.png"));
    definition.color[0] = 0.005f;
    definition.color[1] = 0.005f;
    definition.color[2] = 0.015f;
    definition.withTint = true;
    definition.particlePositionDispersion = 2.21f;
    definition.particleSpeedDispersion = 1.93f;
    definition.decayRate = 0.001f / 0.17f;
    definition.particleScale = 0.052f;
    definition.particleSizeDispersion = 0.014f;
    definition.particleShipSpeedCoefficient = 0.55f;
    definition.initialParticleOpacity = 1f;
    definition.lightDecay = 0.69f;
  }

  public ParticleSmog(float x, float y, float speedX, float speedY) {
    super(definition, x, y, speedX, speedY);
  }

  @Override
  public void draw(Batch batch) {
    def.particleTexture.setColor(
            whiteTint + def.color[0] * (1 - whiteTint),
            whiteTint + def.color[1] * (1 - whiteTint),
            whiteTint + def.color[2] * (1 - whiteTint),
    opacity);
    def.particleTexture.setScale(scale * (2 - opacity));
    def.particleTexture.setCenter(x, y);
    def.particleTexture.draw(batch);
  }
}
