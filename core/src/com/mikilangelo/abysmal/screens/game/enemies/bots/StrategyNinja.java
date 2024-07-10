package com.mikilangelo.abysmal.screens.game.enemies.bots;

import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_WIDTH;

import com.badlogic.gdx.math.MathUtils;
import com.mikilangelo.abysmal.screens.game.actors.ship.Ship;
import com.mikilangelo.abysmal.screens.game.actors.ship.Turret;
import com.mikilangelo.abysmal.shared.tools.CalculateUtils;

public class StrategyNinja extends BotStrategy {

  private float targetAngle = MathUtils.random();
  private float currentDirectionAngle = MathUtils.random();

  @Override
  public void perform(Ship bot, float playerX, float playerY, float playerAngle, float delta) {
    currentDirectionAngle = CalculateUtils.simpleDefineAngle((playerX - bot.x) / bot.distance, (playerY - bot.y) / bot.distance, playerAngle);
    targetAngle = recalculateAim(bot, currentDirectionAngle);

    bot.control(targetAngle, 1, delta);

    if (bot.distance < SCREEN_WIDTH * 0.4f * bot.def.maxZoom) {
      boolean needToShotTurrets = false;
      for (Turret t : bot.turrets) {
        t.control(currentDirectionAngle);
        if (Math.abs((t.angle + bot.angle) % MathUtils.PI2 - t.newAngle) < 0.011f) {
          needToShotTurrets = true;
          break;
        }
      }
      if (needToShotTurrets) {
        bot.shotByTurrets();
      }
      if (!hasDynamicHindrances && Math.abs(currentDirectionAngle - bot.angle) < 0.015f) {
        bot.shotDirectly();
      }
    }
  }

}
