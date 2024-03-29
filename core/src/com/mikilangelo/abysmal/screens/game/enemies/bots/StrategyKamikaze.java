package com.mikilangelo.abysmal.screens.game.enemies.bots;

import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_WIDTH;

import com.badlogic.gdx.math.MathUtils;
import com.mikilangelo.abysmal.screens.game.actors.ship.Ship;
import com.mikilangelo.abysmal.screens.game.actors.ship.Turret;
import com.mikilangelo.abysmal.shared.tools.CalculateUtils;

public class StrategyKamikaze extends BotStrategy {

  private float biasAngle;
  private final float biasRadius;
  private final float attackDistance;
  private final float biasSpeed;
  private float targetAngle = 0.5f;
  private float aimAngle = 0;

  StrategyKamikaze() {
    this.biasAngle = MathUtils.random(0, MathUtils.PI2);
    this.biasRadius = MathUtils.random(13f, 35f);
    final float sqrt = (float) Math.sqrt(biasRadius);
    final float s = MathUtils.PI / MathUtils.random(50f, 450f) / sqrt;
    this.biasSpeed = biasAngle > MathUtils.PI ? s : -s;
    attackDistance = 4.5f + (sqrt * 0.5f + biasRadius * 0.5f) * 0.45f;
  }


  @Override
  public void perform(Ship bot, float playerX, float playerY, float playerAngle, float delta) {
    biasAngle = biasAngle + biasSpeed;
    if (biasAngle > 6.28318f) {
      biasAngle %= 628318f;
    } else if (biasAngle < -6.28318f) {
      biasAngle += 6.28318f;
    }
    if (bot.distance > attackDistance) {
      aimAngle = bot.bodyData.health > 10 + bot.def.health * 0.35f ?
              CalculateUtils.simpleDefineAngle(
              (playerX + biasRadius * (bot.distance - attackDistance) / bot.distance * MathUtils.cos(biasAngle) - bot.x) / bot.distance,
              (playerY + biasRadius * (bot.distance - attackDistance) / bot.distance * MathUtils.sin(biasAngle) - bot.y) / bot.distance,
              bot.angle) :
              (3.14f + CalculateUtils.simpleDefineAngle(
                      (playerX - bot.x) / bot.distance,
                      (playerY - bot.y) / bot.distance, targetAngle)
              ) % MathUtils.PI2;
      // bot.control((aimAngle + 0.5f / (0.1f + bot.distance - attackDistance)) % MathUtils.PI2, 1, delta);
      control(bot, aimAngle, 1, delta);
    }
    if (bot.distance < SCREEN_WIDTH * 0.4f * bot.def.maxZoom) {
      targetAngle = CalculateUtils.simpleDefineAngle((playerX - bot.x) / bot.distance, (playerY - bot.y) / bot.distance, targetAngle);
      if (bot.distance < attackDistance) {
        control(bot, targetAngle, 1, delta);
      }
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
      if (!hasDynamicHindrances && Math.abs(targetAngle - bot.angle) < 0.013f ||
          (bot.distance < biasRadius && Math.abs(targetAngle - bot.angle) < 0.03f)
      ) {
        bot.shotDirectly();
      }
    }

  }
}
