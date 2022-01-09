package com.mikilangelo.abysmal.models.game.extended;

import static com.mikilangelo.abysmal.tools.Geometry.defineAngle;

import com.mikilangelo.abysmal.components.repositories.LasersRepository;
import com.mikilangelo.abysmal.models.definitions.TurretDef;
import com.mikilangelo.abysmal.models.game.Ship;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.TimeUtils;

public class Turret {
  public TurretDef definition;
  public float angle;
  public float newAngle;
  protected long lastShotTime = 0;
  protected String generationId;
  protected float x;
  protected float y;

  public Turret(TurretDef def, String generationId) {
    this.definition = def;
    this.angle = 0;
    this.newAngle = MathUtils.PI / 2;
    this.generationId = generationId;
  }

  public void control(float direction) {
    this.newAngle = direction;
  }

  public void move(float shipAngle, float shipX, float shipY, float delta) {
    final float absoluteAngle = (angle + shipAngle) % MathUtils.PI2;
    if (absoluteAngle != this.newAngle) {
      if (MathUtils.round(absoluteAngle * (10 - definition.rotationSpeed))
              == MathUtils.round(newAngle * (10 - definition.rotationSpeed))
      ) {
        this.angle = newAngle - shipAngle;
      } else {
        if ((absoluteAngle - this.newAngle + MathUtils.PI2) % MathUtils.PI2 <
                (newAngle - absoluteAngle + MathUtils.PI2) % MathUtils.PI2
        ) {
          this.angle -= definition.rotationSpeed;
        } else {
          this.angle += definition.rotationSpeed;
        }
      }
      this.angle = (this.angle + MathUtils.PI2) % MathUtils.PI2;
    }
    x = shipX + definition.positionY * MathUtils.cos(shipAngle + MathUtils.PI / 2) +
            definition.positionX * MathUtils.cos(shipAngle);
    y = shipY + definition.positionY * MathUtils.sin(shipAngle + MathUtils.PI / 2) +
            definition.positionX * MathUtils.sin(shipAngle);
  }

  public void draw(Batch batch, float shipAngle) {
    definition.texture.setCenter(x, y);
    definition.texture.setRotation((shipAngle + angle) * MathUtils.radiansToDegrees);
    definition.texture.draw(batch);
  }

  public void shot(Ship ship, float soundScale) {
    if (ship.ammo < definition.lasersAmount) {
      return;
    }
    ship.ammo -= definition.lasersAmount;
    final long newShotTime = TimeUtils.millis();
    if (newShotTime - lastShotTime < definition.shotInterval) {
      return;
    }
    final float maxLeftLaser = -(definition.lasersAmount - 1) * 0.5f * definition.lasersDistance;
    for (byte i = 0; i < definition.lasersAmount; i++) {
      final float addCos = (maxLeftLaser + definition.lasersDistance * i) * MathUtils.cos(angle + ship.angle + 1.57078f);
      final float addSin = (maxLeftLaser + definition.lasersDistance * i) * MathUtils.sin(angle + ship.angle + 1.57078f);
      Laser l = new Laser(
              definition.laserDefinition, x + addCos, y + addSin,
              angle + ship.angle,
              ship.velocity.x,
              ship.velocity.y,
              generationId, ship.bodyId);
      LasersRepository.addTurret(l);
    }
    definition.laserDefinition.sound.play(soundScale);
//    ship.body.applyLinearImpulse(
//            -1 * MathUtils.cos(angle + ship.angle),
//            -1 * MathUtils.sin(angle + ship.angle),
//            x, y, true);
    this.lastShotTime = newShotTime;
  }
}
