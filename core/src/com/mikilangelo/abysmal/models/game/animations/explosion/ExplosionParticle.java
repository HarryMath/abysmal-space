package com.mikilangelo.abysmal.models.game.animations.explosion;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class ExplosionParticle {
  private final Sprite texture;
  private final int totalFrames;
  private int iterationsLeft;
  private final float x, y;
  private final float size;
  private final boolean isSmoke;

  public ExplosionParticle(Sprite sprite, float size, float x, float y, int lifeTime) {
    this(sprite, size, x, y, lifeTime, false);
  }

  public ExplosionParticle(Sprite sprite, float size, float x, float y, int lifeTime, boolean isSmoke) {
    this.texture = sprite;
    this.iterationsLeft = totalFrames = lifeTime;
    this.size = size;
    this.x = x;
    this.y = y;
    this.isSmoke = isSmoke;
  }

  /**
   * return true if there are frames left;
   **/
  public boolean draw(Batch batch) {
    texture.setScale(size * (0.5f + 0.5f * iterationsLeft / totalFrames) / texture.getHeight());
    texture.setCenter(x, y);
    if (isSmoke) {
      float lightness = 0.9f * iterationsLeft / totalFrames;
      if (lightness > 0.8f) {
        lightness = lightness * 1.25f - 0.8f;
      } else {
        lightness = 0;
      }
      texture.setColor(lightness, lightness, lightness, 1);
    }
    texture.setAlpha(0.2f + 0.8f * iterationsLeft / totalFrames);
    texture.draw(batch);
    iterationsLeft -= 1;
    return iterationsLeft > 0;
  }
}
