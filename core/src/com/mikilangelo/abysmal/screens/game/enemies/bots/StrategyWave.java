package com.mikilangelo.abysmal.screens.game.enemies.bots;

import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_WIDTH;

import com.badlogic.gdx.math.MathUtils;
import com.mikilangelo.abysmal.screens.game.actors.ship.Ship;
import com.mikilangelo.abysmal.screens.game.actors.ship.Turret;
import com.mikilangelo.abysmal.shared.tools.CalculateUtils;

public class StrategyWave implements BotStrategy {

  private float biasAngle = 0;
  private final float maxBias = MathUtils.random(0.6f, 1.03f);
  private final float biasSpeed = MathUtils.random(0.0055f, 0.01f);
  private boolean angleRotateRight = false;
  private float targetAngle;


  @Override
  public void perform(Ship bot, float playerX, float playerY, float playerAngle, float delta) {
    targetAngle = CalculateUtils.simpleDefineAngle((playerX - bot.x) / bot.distance, (playerY - bot.y) / bot.distance, targetAngle);
    if (bot.distance > bot.def.radarPower * 0.7f) {
      bot.control(targetAngle, 1, delta);
      biasAngle = 0;
    } else {
      if (angleRotateRight) {
        if (biasAngle > maxBias) {
          angleRotateRight = false;
        } else {
          biasAngle += biasSpeed;
        }
      } else {
        if (biasAngle < -maxBias) {
          angleRotateRight = true;
        } else {
          biasAngle -= biasSpeed;
        }
      }
      bot.control((MathUtils.PI2 + targetAngle + biasAngle * (0.2f + bot.distance / bot.def.radarPower / 0.4f * 0.8f)) % MathUtils.PI2, 1, delta);
    }

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
      if (Math.abs(targetAngle - bot.angle) < 0.015f) {
        bot.shotDirectly();
      }
    }
  }
}
