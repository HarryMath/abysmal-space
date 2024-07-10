package com.mikilangelo.abysmal.shared.tools;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikilangelo.abysmal.shared.basic.V;

public abstract class CalculateUtils {

  private static final float PI = MathUtils.PI;
  private static final float halfPI = PI / 2;
  private static final float PI2 = 2 * PI;
  private static final float PI4 = 2 * PI2;

  public static String uid() {
    final long timestamp = System.currentTimeMillis();
    return new StringBuilder()
            .append((char) (1 + timestamp % 127))
            .append((char) MathUtils.random(1, 127))
            .append((char) MathUtils.random(1, 127))
            .append((char) MathUtils.random(1, 127))
            .toString();
  }

  public static V normal(float originX, float originY, float length) {
    if (originX < 0.000001 && originX > -0.000001) {
      return new V(length, 0);
    } else if (originY < 0.000001 && originY > -0.000001) {
      return new V(0, length);
    } else {
      float y = - (originX / originY);
      float len = (float) Math.hypot(y, 1);
      y = y * length / len;
      float x = length / len;
      return new V(x, y);
    }
  }

  public static float projection(float originX, float originY, float vX, float vY) {
    return projection(
            originX, originY, vX, vY,
            (float) Math.hypot(originX, originY)
    );
  }

  public static float projection(
          float originX, float originY,
          float vX, float vY,
          float originLen
  ) {
    return (originX * vX + originY * vY) / originLen;
  }

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
    return (res + PI4) % PI2;
  }

  public static boolean testProbability(float p) {
    return MathUtils.random(1f) <= p;
  }

  public static float simpleDefineAngle(float cos, float sin, float angle) {
    if (cos == 0 && sin == 0) {
      return angle;
    }
    if (sin >= 1) {
      return halfPI;
    } else if (sin <= -1) {
      return PI + halfPI;
    }
    final float res = cos >= 0 ? MathUtils.asin(sin) : PI - MathUtils.asin(sin);
    return (res + PI4) % PI2;
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
    a1 = normalizeAngle(a1);
    a2 = normalizeAngle(a2);
    float r1, r2;
    if (a1 > a2) {
      r1 = a1 - a2;
      r2 = a2 - a1 + PI2;
      if (r1 < r2) {
        return a2 + r1 * w2;
      } else {
        return a1 + r2 * w1;
      }
    } else {
      r1 = a2 - a1;
      r2 = a1 - a2 + PI2;
      if (r1 < r2) {
        return a1 + r1 * w2;
      } else {
        return a2 + r2 * w1;
      }
    }
  }

  public static float defineAngle(Vector2 p1, Vector2 p2) {
    return defineAngle(p1.x - p2.x, p1.y - p2.y, 0);
  }

  public static float normalizeAngle(float a) {
    return (a + PI4) % PI2;
  }
}
