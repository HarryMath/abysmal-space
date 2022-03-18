package com.mikilangelo.abysmal.screens.game.actors.decor.animations.explosion;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class ExplosionParticle {
  private final Sprite texture;
  private final int totalFrames;
  private int iterationsLeft;
  private final float x, y;
  private final float size;
  private boolean isSmoke;
  private final float darkness;

  public ExplosionParticle(Sprite sprite, float size, float x, float y, int lifeTime) {
    this(sprite, size, x, y, lifeTime, 0);
    isSmoke = false;
  }

  public ExplosionParticle(Sprite sprite, float size, float x, float y, int lifeTime, float darkness) {
    this.texture = sprite;
    this.iterationsLeft = totalFrames = lifeTime;
    this.size = size;
    this.x = x;
    this.y = y;
    this.darkness = 0.5f + (1 - darkness) * 0.5f;
    this.isSmoke = true;
  }

  /**
   * return true if there are frames left;
   **/
  public boolean draw(Batch batch) {
    texture.setScale(size * (0.5f + 0.5f * iterationsLeft / totalFrames) / texture.getHeight());
    texture.setCenter(x, y);
    if (isSmoke) {
      float lightness = 0.9f * iterationsLeft / totalFrames;
      if (lightness > 0.68f) {
        lightness = (lightness - 0.8f) * 5 * darkness;
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
