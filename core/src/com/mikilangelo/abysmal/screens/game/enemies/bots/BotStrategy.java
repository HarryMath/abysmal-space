package com.mikilangelo.abysmal.screens.game.enemies.bots;

import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_HEIGHT;
import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_WIDTH;

import com.badlogic.gdx.math.MathUtils;
import com.mikilangelo.abysmal.screens.game.GameScreen;
import com.mikilangelo.abysmal.screens.game.actors.fixtures.Asteroid;
import com.mikilangelo.abysmal.screens.game.actors.ship.Ship;
import com.mikilangelo.abysmal.screens.game.enemies.Enemy;
import com.mikilangelo.abysmal.shared.Settings;
import com.mikilangelo.abysmal.shared.basic.V;
import com.mikilangelo.abysmal.shared.repositories.AsteroidsRepository;
import com.mikilangelo.abysmal.shared.tools.CalculateUtils;

public abstract class BotStrategy {
  abstract void perform(Ship bot, float playerX, float playerY, float playerAngle, float delta);

  private float minHindrance2D = 9999999;
  private boolean hasHindrances = false;
  private V subAim = new V(0, 0);
  protected boolean hasDynamicHindrances = false;

  protected float recalculateAim(Ship bot, float target) {
    final float angle = CalculateUtils.avgAngle(bot.angle, 0.4f, target, 0.6f);
    final float scanDistance = Math.min(bot.distance, bot.def.maxZoom * SCREEN_HEIGHT * 2);
    final float aimX = bot.x + MathUtils.cos(angle) * scanDistance;
    final float aimY = bot.y + MathUtils.sin(angle) * scanDistance;
    return this.recalculateAim(bot, aimX, aimY, angle, scanDistance);
  }

  private float recalculateAim(Ship bot, float aimX, float aimY, float angle, float scanDistance) {
    subAim = new V(aimX, aimY);
    minHindrance2D = 9999999;
    hasHindrances = false;
    hasDynamicHindrances = false;

    for (Enemy e: ((BotsProcessor) GameScreen.enemiesProcessor).bots) {
      if (bot.generationId.equals(e.ship.generationId)) {
        continue;
      }
      hasDynamicHindrances = checkHindrance(
              bot, aimX, aimY,
              e.ship.x, e.ship.y, e.ship.def.bodyScale,
              scanDistance
      ) || hasDynamicHindrances;
    }
    for (Asteroid a: AsteroidsRepository.asteroids) {
      if (a.asteroidTypeId <= Asteroid.smallAmount) {
        continue;
      }
      checkHindrance(
              bot, aimX, aimY,
              a.x, a.y, a.getSize(),
              scanDistance
      );
    }

    if (!hasHindrances && Settings.debug) {
      bot.aimY = bot.y + MathUtils.sin(angle) * 10;
      bot.aimX = bot.x + MathUtils.cos(angle) * 10;
      bot.normalX = 0;
      bot.normalY = 0;
    }
    return hasHindrances ?
            CalculateUtils.defineAngle(subAim.x - bot.x, subAim.y - bot.y, angle) : angle;
  }

  protected boolean checkHindrance(
          Ship bot, float aimX, float aimY,
          float hX, float hY, float hSize, float scanDistance
  ) {
    float dxFirst = hX - bot.x;
    float dyFirst = hY - bot.y;
    if (
            Math.abs(dxFirst) <= scanDistance &&
            Math.abs(dyFirst) <= scanDistance
    ) {
      float dxSecond = aimX - hX;
      float dySecond = aimY - hY;
      float dxAim = aimX - bot.x;
      float dyAim = aimY - bot.y;

      float d2First = CalculateUtils.squaresSum(dxFirst, dyFirst);
      float d2Second = CalculateUtils.squaresSum(dxSecond, dySecond);
      float d2Aim = CalculateUtils.squaresSum(dxAim, dyAim);

      final float maxAllowedDistance = (hSize + bot.def.bodyScale) * 0.65f;

      if (d2First + d2Second < d2Aim + maxAllowedDistance) {

        if (hasHindrances && minHindrance2D < d2First) {
          return false; // There is hindrance which is closer
        }

        final float aimD = (float) Math.sqrt(d2Aim);

        final float projection = CalculateUtils.projection(dxAim, dyAim, dxFirst, dyFirst, aimD);

        final float projectionPointX = bot.x + (aimX - bot.x) / aimD * projection;
        final float projectionPointY = bot.y + (aimY - bot.y) / aimD * projection;

        // square distance from hindrance to initial path
        final float d2Path = CalculateUtils.squaresSum(hX - projectionPointX, hY - projectionPointY);

        if (d2Path > maxAllowedDistance * maxAllowedDistance ) {
          return false; // distance is ok. False hindrance
        }

        minHindrance2D = d2First;
        hasHindrances = true;
        final V normal = CalculateUtils.normal(dxAim, dyAim,
                maxAllowedDistance - (float) Math.sqrt(d2Path)
        );

        subAim.x = projectionPointX + normal.x;
        subAim.y = projectionPointY + normal.y;

        // float D1 = (hX - bot.x) * dyAim - (hY - bot.y) * dxAim;
        float D1 = dxFirst * dyAim - dyFirst * dxAim;
        float D2 = (subAim.x - bot.x) * dyAim - (subAim.y - bot.y) * dxAim;
        if (D1 * D2 > 0) {
          // if correction-normal and hindrance at the same side
          normal.x = -normal.x;
          normal.y = -normal.y;
        }
        subAim.x = projectionPointX + normal.x;
        subAim.y = projectionPointY + normal.y;

        if (Settings.debug) {
          bot.normalX = normal.x;
          bot.normalY = normal.y;
          bot.aimX = projectionPointX;
          bot.aimY = projectionPointY;
        }
        return true;
      }
    }
    return false;
  }

  protected void control(Ship bot, float direction, float power, float delta) {
    bot.control(recalculateAim(bot, direction), power, delta);
  }

  @Deprecated
  protected void control(Ship bot, float aimX, float aimY, float power, float delta) {
    bot.control(recalculateAim(bot, aimX, aimY, 0, bot.def.maxZoom * SCREEN_WIDTH), power, delta);
  }
}
