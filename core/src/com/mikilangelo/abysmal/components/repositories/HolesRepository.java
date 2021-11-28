package com.mikilangelo.abysmal.components.repositories;

import com.mikilangelo.abysmal.models.game.animations.BlackHole;
import com.mikilangelo.abysmal.tools.Geometry;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public abstract class HolesRepository {

  private static final Array<BlackHole> holes = new Array<>();
  private static int nearestHoleIndex = 0;
  private static final float maxDistance = 1500;


  public static void generateHoles(float playerX, float playerY) {
    for (byte i = 0; i < 3; i++) {
      generateHole(playerX, playerY);
    }
  }

  public static void setUpShader(ShaderProgram shader, float playerX, float playerY, float zoom) {
    updateHoles(playerX, playerY);
    holes.get(nearestHoleIndex).setUpShader(shader, playerX, playerY, zoom);
  }

  private static void updateHoles(float playerX, float playerY) {
    float minDistance = maxDistance;
    nearestHoleIndex = 0;
    for (int i = 0; i < holes.size; i++) {
      float distance = Geometry.distance(playerX, playerY, holes.get(i).x, holes.get(i).y);
      if (distance <= minDistance) {
        minDistance = distance;
        nearestHoleIndex = i;
      } else if (distance > maxDistance) {
        holes.removeIndex(i--);
        generateHole(playerX, playerY);
      }
    }
  }

  private static void generateHole(float playerX, float playerY) {
    final float x = MathUtils.random(playerX - maxDistance * 0.5f, playerX + maxDistance * 0.5f);
    final float y = MathUtils.random(playerY - maxDistance * 0.5f, playerY + maxDistance * 0.5f);
    if (holes.size != 0 && Geometry.distance(holes.get(nearestHoleIndex).x,
            holes.get(nearestHoleIndex).y, x, y) < maxDistance * 0.5f) {
        return;
    }
    holes.add(new BlackHole(x, y, 0.95f));
  }

  public static void clear() {
    holes.clear();
  }

}
