package com.mikilangelo.abysmal.enemies.bots;

import static com.mikilangelo.abysmal.ui.screens.GameScreen.SCREEN_WIDTH;

import com.badlogic.gdx.math.MathUtils;
import com.mikilangelo.abysmal.models.game.Ship;
import com.mikilangelo.abysmal.models.game.extended.Turret;
import com.mikilangelo.abysmal.tools.Geometry;

public class StrategyWave implements BotStrategy {

  private float biasAngle = 0;
  private final float maxBias = MathUtils.random(0.6f, 1.03f);
  private final float biasSpeed = MathUtils.random(0.0055f, 0.01f);
  private boolean angleRotateRight = false;
  private float targetAngle;


  @Override
  public void perform(Ship bot, float playerX, float playerY, float playerAngle, float delta) {
    targetAngle = Geometry.simpleDefineAngle((playerX - bot.x) / bot.distance, (playerY - bot.y) / bot.distance, targetAngle);
    if (bot.distance > bot.definition.radarPower * 0.7f) {
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
      bot.control((MathUtils.PI2 + targetAngle + biasAngle * (0.2f + bot.distance / bot.definition.radarPower / 0.4f * 0.8f)) % MathUtils.PI2, 1, delta);
    }

    if (bot.distance < SCREEN_WIDTH * 0.4f * bot.definition.maxZoom) {
      boolean needToShotTurrets = false;
      for (Turret t : bot.turrets) {
        t.control(targetAngle);
        if (Math.abs((t.angle + bot.angle) % MathUtils.PI2 - t.newAngle) < 0.011f) {
          needToShotTurrets = true;
          break;
        }
      }
      if (needToShotTurrets || Math.abs(targetAngle - bot.angle) < 0.013f) {
        bot.shot();
      }
    }
  }
}
