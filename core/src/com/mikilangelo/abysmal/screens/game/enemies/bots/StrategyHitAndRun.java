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
  private final float attackDistance = 40f + MathUtils.random(0, 15);

  @Override
  public void perform(Ship bot, float playerX, float playerY, float playerAngle, float delta) {
    targetAngle = CalculateUtils.simpleDefineAngle((playerX - bot.x) / bot.distance, (playerY - bot.y) / bot.distance, targetAngle);
    if (isAttacking) {
      if (bot.distance > attackDistance) {
        bot.control(targetAngle, 1, delta);
      } else if (bot.distance > 21 || Math.abs(bot.angle - playerAngle) <= 1) {
        bot.control(targetAngle, 0.8f + bot.distance / attackDistance * 0.2f, delta);
      } else {
        isAttacking = false;
        leavingAngle = 0;
      }
    } else {
      if (bot.distance > attackDistance * 1.2f + 5 ||
              (bot.distance > attackDistance * 0.9f && leavingAngle >= 3f)
      ) {
        isAttacking = bot.bodyData.health > 7 + bot.def.health * 0.15f;
      }
      if (leavingAngle < 3.14f) {
        leavingAngle = leavingAngle * 1.03f + 0.008f;
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
      if (needToShotTurrets) {
        bot.shotByTurrets();
      }
      if (Math.abs(targetAngle - bot.angle) < 0.03f ||
          (bot.distance < 21 && Math.abs(targetAngle - bot.angle) < 0.2f)
      ) {
        bot.shotDirectly();
      }
    }
  }

}
