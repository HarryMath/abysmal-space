package com.mikilangelo.abysmal.screens.game.enemies.bots;

import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_WIDTH;

import com.badlogic.gdx.math.MathUtils;
import com.mikilangelo.abysmal.screens.game.actors.ship.Ship;
import com.mikilangelo.abysmal.screens.game.actors.ship.Turret;
import com.mikilangelo.abysmal.shared.tools.CalculateUtils;

public class StrategyHitAndRun implements BotStrategy {

  private float targetAngle = 0;
  private boolean isAttacking = false;
  private float leavingAngle = 0;

  @Override
  public void perform(Ship bot, float playerX, float playerY, float playerAngle, float delta) {
    targetAngle = CalculateUtils.simpleDefineAngle((playerX - bot.x) / bot.distance, (playerY - bot.y) / bot.distance, targetAngle);
    if (isAttacking) {
      if (bot.distance > 50) {
        bot.control(targetAngle, 1, delta);
      } else if (bot.distance > 20 || Math.abs(bot.angle - playerAngle) <= 1.4f) {
        bot.control(targetAngle, 0.7f + bot.distance / 50 * 0.3f, delta);
      } else {
        isAttacking = false;
        leavingAngle = 0;
      }
    } else {
      if (bot.distance > bot.def.radarPower * 0.33f ||
              (bot.distance > 11 && leavingAngle >= MathUtils.PI * 0.4f)
      ) {
        isAttacking = bot.bodyData.health > 10 + bot.def.health * 0.35f;
      }
      if (leavingAngle < MathUtils.PI * 0.3f) {
        leavingAngle = leavingAngle * 1.1f + 0.01f;
      }
      bot.control((leavingAngle + targetAngle) % MathUtils.PI2, 1, delta);
    }
    // try shot
    if (bot.distance < SCREEN_WIDTH * 0.4f * bot.def.maxZoom) {
      boolean needToShotTurrets = false;
      for (Turret t : bot.turrets) {
        t.control(targetAngle);
        if (Math.abs((t.angle + bot.angle) % MathUtils.PI2 - t.newAngle) < 0.011f) {
          needToShotTurrets = true;
          break;
        }
      }
      if (needToShotTurrets || Math.abs(targetAngle - bot.angle) < 0.03f ||
              (bot.distance < 21 && Math.abs(targetAngle - bot.angle) < 0.2f)
      ) {
        bot.shot();
      }
    }
  }

}
