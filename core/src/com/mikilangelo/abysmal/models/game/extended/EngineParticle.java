package com.mikilangelo.abysmal.models.game.extended;


import com.mikilangelo.abysmal.models.definitions.EngineDef;
import com.mikilangelo.abysmal.models.game.basic.DynamicObject;
import com.mikilangelo.abysmal.ui.screens.GameScreen;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;

public class EngineParticle implements DynamicObject {
  final EngineDef definition;
  public float x;
  public float y;
  public float opacity = 0.1f;
  public float scale;
  float speedX, speedY;
  boolean stateRaising = true;

  final float speedCoefficient;
  final float dyingSpeed;

  public EngineParticle(EngineDef def, float x, float y, float speedX, float speedY) {
    this.definition = def;
    this.speedX = speedX * def.particleShipSpeedCoefficient +
            (float) (Math.random() - 0.5f) * def.particleSpeedDispersion;
    this.speedY = speedY * def.particleShipSpeedCoefficient +
            (float) (Math.random() - 0.5f) * def.particleSpeedDispersion;
    this.x = x + (float) (Math.random() - 0.5f) * (def.particlePositionDispersion);
    this.y = y + (float) (Math.random() - 0.5f) * (def.particlePositionDispersion);
    float size = def.particleScale +
            MathUtils.random(-def.particleSizeDispersion, def.particleSizeDispersion);
    this.scale = GameScreen.SCREEN_HEIGHT * size / def.particleTexture.getHeight();
    speedCoefficient = 3 / (3 + scale);
    dyingSpeed = 0.016f / definition.particleLifeTime;
  }


  public void move(float delta) {
    if (opacity == 0) return;
    y += speedY * delta * speedCoefficient;
    x += speedX * delta * speedCoefficient;
    this.speedX *= 0.987f;
    this.speedY *= 0.987f;
    if (stateRaising) {
      if (opacity >= 0.9) {
        this.opacity = 1;
        this.stateRaising = false;
      } else {
        this.opacity += 0.1;
      }
    } else {
      opacity -= dyingSpeed;
      if (opacity <= 0) {
        this.opacity = 0;
        this.speedY = 0;
        this.speedX = 0;
      }
    }
  }

  public void draw(Batch batch) {
    definition.particleTexture.setAlpha(opacity);
    definition.particleTexture.setScale(scale);
    definition.particleTexture.setCenter(x, y);
    definition.particleTexture.draw(batch);
  }
}
