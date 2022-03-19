package com.mikilangelo.abysmal.screens.game.enemies.bots;

import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_WIDTH;

import com.badlogic.gdx.math.MathUtils;
import com.mikilangelo.abysmal.screens.game.actors.ship.Ship;
import com.mikilangelo.abysmal.screens.game.actors.ship.Turret;
import com.mikilangelo.abysmal.shared.tools.CalculateUtils;

public class StrategySniper implements BotStrategy {

  private final float attackDistance = MathUtils.random(11f, 30f);
  private final float aheadCoefficient = MathUtils.random(0.07f, 0.31f);
  private float targetAngle;

  @Override
  public void perform(Ship bot, float playerX, float playerY, float playerAngle, float delta) {
    targetAngle = CalculateUtils.simpleDefineAngle(
            (playerX + bot.distance * aheadCoefficient * MathUtils.cos(playerAngle) - bot.x) / bot.distance,
            (playerY + bot.distance * aheadCoefficient * MathUtils.sin(playerAngle) - bot.y) / bot.distance,
            targetAngle
    );
    bot.control(targetAngle,
            bot.distance > attackDistance * 1.2f ? 1 :
            bot.distance > attackDistance * 0.4f ?
            (bot.distance - attackDistance * 0.4f) / (attackDistance * (1.2f - 0.4f)) : 0,
            delta
    );
    if (bot.distance < SCREEN_WIDTH * 0.4f * bot.def.maxZoom) {
      boolean needToShotTurrets = false;
      for (Turret t : bot.turrets) {
        t.control(targetAngle);
        if (Math.abs((t.angle + bot.angle) % MathUtils.PI2 - t.newAngle) < 0.011f) {
          needToShotTurrets = true;
          break;
        }
      }
      if (needToShotTurrets) {
        bot.shotByTurrets();
      }
      if (Math.abs(targetAngle - bot.angle) < 0.01f ||
         (bot.distance < attackDistance && Math.abs(targetAngle - bot.angle) < 0.02f)
      ) {
        bot.shotDirectly();
      }
    }
  }
}
