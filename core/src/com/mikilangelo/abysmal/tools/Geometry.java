package com.mikilangelo.abysmal.tools;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public abstract class Geometry {

  private static final float PI = MathUtils.PI;
  private static final float halfPI = MathUtils.PI / 2;
  private static final float PI2 = MathUtils.PI2;

  public static float defineAngle(float x, float y, float angle) {
    if (x == 0 && y == 0) {
      return angle;
    }
    float res;
    if (x >= 0) {
      res = MathUtils.asin(y / (float) Math.hypot(x, y));
    } else {
      res = PI - MathUtils.asin(y / (float) Math.hypot(x, y));
    }
    return (res + PI2) % PI2;
  }

  public static float simpleDefineAngle(float cos, float sin, float angle) {
    if (cos == 0 && sin == 0) {
      return angle;
    }
    final float res = cos >= 0 ? MathUtils.asin(sin) : PI - MathUtils.asin(sin);
    return (res + PI2) % PI2;
  }

  public static float squaresSum(float x, float y) {
    return x * x + y * y;
  }

  public static float distance(float x1, float y1, float x2, float y2) {
    return (float) Math.hypot(x2 - x1, y2 - y1);
  }

  public static float distance(Vector2 p1, Vector2 p2) {
    return (float) Math.hypot(p1.x - p2.x, p1.y - p2.y);
  }

  public static float avgAngle(float a1, float w1, float a2, float w2) {
    final float cos = (MathUtils.cos(a1) * w1 + MathUtils.cos(a2) * w2) / (w1 + w2);
    final float sin = (MathUtils.sin(a1) * w1 + MathUtils.sin(a2) * w2) / (w1 + w2);
    return simpleDefineAngle(cos, sin, a1);
  }

  public static float defineAngle(Vector2 p1, Vector2 p2) {
    return defineAngle(p1.x - p2.x, p1.y - p2.y, 0);
  }
}
