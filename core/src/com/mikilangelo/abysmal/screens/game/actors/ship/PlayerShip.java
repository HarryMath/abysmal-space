package com.mikilangelo.abysmal.screens.game.actors.ship;

import com.mikilangelo.abysmal.shared.defenitions.ShipDef;

public class PlayerShip extends Ship {

  public static float X = 0;
  public static float Y = 0;
  public static float SPEED = 0;

  private long lastSpeedUp = 0;
  private float speedTimeLeft = 0;
  private float speedCoefficient = 1;

  public PlayerShip(ShipDef def, float x, float y) {
    super(def, x, y, false, x, y);
  }

  public float getSpeedReloadTime(long currentTime) {
    return (def.speedRechargeTimeMs + lastSpeedUp - currentTime) / 1000f;
  }

  public void speedUp() {
    final long currentTime = System.currentTimeMillis();
    if (currentTime - lastSpeedUp > def.speedRechargeTimeMs) {
      this.speedCoefficient = def.speedUpCoefficient;
      speedTimeLeft = def.speedTimeS;
      lastSpeedUp = currentTime;
    }
  }

  @Override
  public void applyImpulse(float power, boolean withParticles) {
    super.applyImpulse(
            power > 0.9f ? power * speedCoefficient : power,
            withParticles);
  }

  @Override
  public void move(float delta) {
    super.move(delta);
    X = this.x;
    Y = this.y;
    SPEED = this.speed;
    if (speedCoefficient > 1) {
      speedTimeLeft -= delta;
      if (speedTimeLeft <= 0) {
        speedCoefficient = 1;
      }
    }
  }

  @Override
  public void shotDirectly() {
    this.shotDirectly(1, 0);
  }

  @Override
  public void shotByTurrets() {
    for (byte i = 0; i < turrets.size; i++) {
      turrets.get(i).shot(this, 1, 0);
    }
  }
}
