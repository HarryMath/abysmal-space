package com.mikilangelo.abysmal.screens.game.actors.ship;

import static com.mikilangelo.abysmal.shared.tools.CalculateUtils.defineAngle;

import com.mikilangelo.abysmal.shared.repositories.LasersRepository;
import com.mikilangelo.abysmal.shared.defenitions.TurretDef;
import com.mikilangelo.abysmal.shared.tools.CalculateUtils;
import com.mikilangelo.abysmal.screens.game.GameScreen;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.mikilangelo.abysmal.shared.tools.Logger;

public class AutomaticTurret extends Turret {

  private Ship target = null;

  public AutomaticTurret(TurretDef def, String generationId) {
    super(def, generationId, 0);
  }

  @Override
  public void control(float direction) {}

  @Override
  public void move(float shipAngle, float shipX, float shipY, float delta) {
    super.move(shipAngle, shipX, shipY, delta);
    if (target != null && target.body != null) {
      final float targetX = target.x + target.body.getLinearVelocity().x * 0.2f;
      final float targetY = target.y + target.body.getLinearVelocity().y * 0.2f;
      final float distance = CalculateUtils.distance(targetX, targetY, this.x, this.y);
      this.log("move", "distance is " + distance);
      if (distance < 80) {
        this.newAngle = defineAngle(targetX - this.x, targetY - this.y, newAngle);
        if (distance < 45 && Math.abs((angle + shipAngle) % MathUtils.PI2 - newAngle) < 0.02f) {
          this.autoShot(shipAngle);
          if (distance < 30) {
            return;
          }
        }
      }
    }
    this.target = GameScreen.enemiesProcessor.getNearestEnemy(shipX, shipY);
  }

  @Deprecated
  private void autoShot(float shipAngle) {
    final long newShotTime = TimeUtils.millis();
    if (newShotTime - lastShotTime < definition.shotInterval) {
      return;
    }
    float maxLeftLaser = -definition.lasersDistance * definition.lasersAmount / 2f + definition.lasersDistance / 2f;
    for (byte i = 0; i < definition.lasersAmount; i++) {
      float addCos = (maxLeftLaser + definition.lasersDistance * i) * MathUtils.cos(this.angle + shipAngle + MathUtils.PI / 2);
      float addSin = (maxLeftLaser + definition.lasersDistance * i) * MathUtils.sin(this.angle + shipAngle + MathUtils.PI / 2);
      Laser l = new Laser(definition.laserDefinition, x + addCos, y + addSin,
              angle + shipAngle, 0, 0, generationId, (short) (-1));
      LasersRepository.addTurret(l);
    }
    definition.laserDefinition.sound.play(1);
    this.lastShotTime = newShotTime;
  }

  @Override
  public void shot(Ship ship, float soundScale, float pan) { }
}
