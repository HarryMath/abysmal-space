package com.mikilangelo.abysmal.shared.tools;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Random {

  private long seed;

  public Random(long seed) {
    // this.seed = seed > 0 ? seed : seed < 0 ? -seed : 13;
    this.seed = seed;
  }

  public int nextInt(int start, int end) {
    seed = (seed * 73129 + 12345) % 1000000;
    return start + ((int) (seed / 7) % (end - start + 1));
  }

  /**
   * @return pseudo-random float from [0, 1]
   */
  public float nextFloat() {
    seed = (seed * 73129 + 12345) % 1000000;
    return ((int) (seed / 7) % 32768) / 32767f;
  }

  /**
   * @return pseudo-random normal-distributed number
   * with M = 0 and dispersion = 1;
   */
  public Vector2 nextGaussian() {
    float r = 0.01f + nextFloat() * 0.99f;
    float angle = nextFloat(0, 6.2831f);
    float sqrt = (float) Math.sqrt(-2 * Math.log(r));
    return new Vector2(
            sqrt * MathUtils.cos(angle),
            sqrt * MathUtils.sin(angle)
    );
  }

  /**
   * @param start - start of the range
   * @param end - end of the range
   * @return pseudo-random float from [start, end]
   */
  public float nextFloat(float start, float end) {
    return start + nextFloat() * (end - start);
  }

  public long getSeed() {
    return seed;
  }
}
