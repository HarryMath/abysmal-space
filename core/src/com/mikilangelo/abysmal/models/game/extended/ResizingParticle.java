package com.mikilangelo.abysmal.models.game.extended;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.mikilangelo.abysmal.models.definitions.EngineDef;

public class ResizingParticle extends EngineParticle {

  public ResizingParticle(EngineDef def, float x, float y, float speedX, float speedY) {
    super(def, x, y, speedX, speedY);
  }

  @Override
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
    def.particleTexture.setScale(scale * (0.4f + 0.6f * opacity));
    def.particleTexture.setCenter(x, y);
    def.particleTexture.draw(batch);
  }
}
