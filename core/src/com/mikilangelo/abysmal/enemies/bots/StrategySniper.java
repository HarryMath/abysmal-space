package com.mikilangelo.abysmal.enemies.bots;

import static com.mikilangelo.abysmal.ui.screens.GameScreen.SCREEN_WIDTH;

import com.badlogic.gdx.math.MathUtils;
import com.mikilangelo.abysmal.models.game.Ship;
import com.mikilangelo.abysmal.models.game.extended.Turret;
import com.mikilangelo.abysmal.tools.Geometry;

public class StrategySniper implements BotStrategy {

  private final float attackDistance = MathUtils.random(9f, 30f);
  private final float aheadCoefficient = MathUtils.random(0.07f, 0.31f);
  private float targetAngle;

  @Override
  public void perform(Ship bot, float playerX, float playerY, float playerAngle, float delta) {
    targetAngle = Geometry.simpleDefineAngle(
            (playerX + bot.distance * aheadCoefficient * MathUtils.cos(playerAngle) - bot.x) / bot.distance,
            (playerY + bot.distance * aheadCoefficient * MathUtils.sin(playerAngle) - bot.y) / bot.distance,
            targetAngle
    );
    bot.control(targetAngle,
            bot.distance > attackDistance * 2 ? 1 : bot.distance > attackDistance ?
                    (bot.distance - attackDistance) / attackDistance : 0,
            delta);
    if (bot.distance < SCREEN_WIDTH * 0.4f * bot.definition.maxZoom) {
      boolean needToShotTurrets = false;
      for (Turret t : bot.turrets) {
        t.control(targetAngle);
        if (Math.abs((t.angle + bot.angle) % MathUtils.PI2 - t.newAngle) < 0.011f) {
          needToShotTurrets = true;
          break;
        }
      }
      if (needToShotTurrets || Math.abs(targetAngle - bot.angle) < 0.01f ||
              (bot.distance < attackDistance && Math.abs(targetAngle - bot.angle) < 0.02f)
      ) {
        bot.shot();
      }
    }
  }
}