package com.mikilangelo.abysmal.models.game.animations.explosion;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class AnimatedExplosion {

  public final float maxRadius;
  public final Array<ExplosionFlame> flames;

  public AnimatedExplosion(Sprite sparkTexture, Sprite smokeTexture, float size, float x, float y) {
    this.maxRadius = size;
    final int bigFlames = MathUtils.random(2, 4);
    final int smallFlames = MathUtils.random(2, 4);
    flames = new Array<>(bigFlames + smallFlames);
    for (int i = 0; i < bigFlames; i++) {
      flames.add(new ExplosionFlame(
              sparkTexture, smokeTexture,
              size * MathUtils.random(0.8f, 1.3f),
              i * MathUtils.PI2 / bigFlames + MathUtils.random(-1f, 1f),
              x, y));
    }
    for (int i = 0; i < smallFlames; i++) {
      flames.add(new ExplosionFlame(
              sparkTexture, smokeTexture,
              size * MathUtils.random(0.21f, 0.4f),
              i * MathUtils.PI2 / smallFlames + MathUtils.random(-0.5f, 0.5f),
              x, y));
    }
  }

  public boolean draw(Batch batch) {
    for(int i = 0; i < flames.size; i++) {
      if (!flames.get(i).draw(batch)) {
        flames.removeIndex(i);
      }
    }
    return flames.size > 0;
  }
}
