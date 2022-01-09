package com.mikilangelo.abysmal.models.game.extended;


import com.mikilangelo.abysmal.models.definitions.EngineDef;
import com.mikilangelo.abysmal.models.game.basic.DynamicObject;
import com.mikilangelo.abysmal.ui.screens.GameScreen;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;

public class EngineParticle implements DynamicObject {
  final EngineDef def;
  public float x;
  public float y;
  public float opacity;
  public float whiteTint = 1;
  public float scale;
  float speedX, speedY;
  boolean stateRaising = true;

  final float speedCoefficient;

  public EngineParticle(EngineDef def, float x, float y, float speedX, float speedY) {
    this.def = def;
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
    opacity = def.initialParticleOpacity;
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
    } else if (opacity > 0) {
      opacity -= def.decayRate;
      if (opacity <= 0) {
        this.opacity = 0;
        this.speedY = 0;
        this.speedX = 0;
      }
    }
    if (def.withTint && whiteTint > 0) {
      whiteTint -= 0.07f;
      if (whiteTint <= 0) {
        whiteTint = 0;
      }
    }
  }

  public void draw(Batch batch) {
    if (def.withTint) {
      def.particleTexture.setColor(
              whiteTint + def.color[0] * (1 - whiteTint),
              whiteTint + def.color[1] * (1 - whiteTint),
              whiteTint + def.color[2] * (1 - whiteTint),
              opacity);
    } else {
      def.particleTexture.setAlpha(opacity);
    }
    def.particleTexture.setScale(scale);
    def.particleTexture.setCenter(x, y);
    def.particleTexture.draw(batch);
  }
}
