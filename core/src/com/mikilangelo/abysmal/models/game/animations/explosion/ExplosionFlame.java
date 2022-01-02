package com.mikilangelo.abysmal.models.game.animations.explosion;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;


public class ExplosionFlame {
  private final float stepSize;
  private float direction, x, y, directionSpeed;
  private final static int lifeTime = 65;
  private final int framesAmount = 40;
  private final float maxSize;
  private int framesPassed = 0;
  private final Sprite spark, smoke;
  private final Array<ExplosionParticle> particles;

  public ExplosionFlame(Sprite spark, Sprite smoke, float length, float direction, float x, float y) {
    this.stepSize = length / framesAmount * MathUtils.random(2f, 2.5f);
    maxSize = length * 0.22f + 1.51f;
    this.spark = spark;
    this.smoke = smoke;
    this.direction = direction;
    this.directionSpeed = 0;
    this.particles = new Array<>(framesAmount);
    this.x = x;
    this.y = y;
  }

  public boolean draw(Batch batch) {
    if (framesPassed <= framesAmount) {
      directionSpeed += MathUtils.random(-0.015f, 0.015f);
      direction += directionSpeed;
      final float d = stepSize * MathUtils.random(0.6f, 1) * (framesAmount - framesPassed) / framesAmount;
      x += d * MathUtils.cos(direction);
      y += d * MathUtils.sin(direction);
      if (framesPassed % 5 == 0) {
        final float scale = (framesAmount - framesPassed) / (float) framesAmount;
        particles.add(new ExplosionParticle(
                smoke, 0.21f + maxSize * scale * scale , x, y,
                (lifeTime / 2 - framesPassed / 3), true));
      }
      framesPassed++;
    } else if (framesPassed == framesAmount + 1) {
      particles.add(new ExplosionParticle(smoke, 0.24f, x, y, (lifeTime / 2 - framesPassed / 3), true));
      particles.add(new ExplosionParticle(spark, 0.22f, x, y, (lifeTime - framesPassed) / 4));
      framesPassed ++;
    }
    for (int i = 0; i < particles.size; i++) {
      if (!particles.get(i).draw(batch)) {
        particles.removeIndex(i);
      }
    }
    if (framesPassed < framesAmount) {
      spark.setScale(0.23f / spark.getHeight());
      spark.setCenter(x, y);
      spark.setAlpha(1);
      spark.draw(batch);
    }
    return particles.size > 0;
  }
}
