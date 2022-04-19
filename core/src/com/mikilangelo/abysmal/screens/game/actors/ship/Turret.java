package com.mikilangelo.abysmal.screens.game.actors.ship;

import com.mikilangelo.abysmal.shared.repositories.LasersRepository;
import com.mikilangelo.abysmal.shared.defenitions.TurretDef;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.mikilangelo.abysmal.screens.game.GameScreen;

public class Turret {
  private static final float halfPI = MathUtils.PI * 0.5f;
  public TurretDef definition;
  private final int gunId;
  public float angle;
  public float newAngle;
  protected long lastShotTime = 0;
  protected long lastSoundPLayTime = 0;
  protected String generationId;
  protected float x;
  protected float y;
  protected long soundId = 0;

  public Turret(TurretDef def, String generationId, int gunId) {
    this.definition = def;
    this.gunId = gunId;
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
    x = shipX + definition.positionY * MathUtils.cos(shipAngle + halfPI) +
            definition.positionX * MathUtils.cos(shipAngle);
    y = shipY + definition.positionY * MathUtils.sin(shipAngle + halfPI) +
            definition.positionX * MathUtils.sin(shipAngle);
  }

  public void draw(Batch batch, float shipAngle) {
    definition.texture.setCenter(x, y);
    definition.texture.setRotation((shipAngle + angle) * MathUtils.radiansToDegrees);
    definition.texture.draw(batch);
  }

  public void playShotSound(float soundScale, float pan) {
    if (soundId > 0) {
      definition.laserDefinition.sound.stop(soundId);
    } // pitch 1 is speed
    soundId = definition.laserDefinition.sound.play(soundScale, 1, pan);
  }

  public void shot(Ship ship, float soundScale, float pan) {
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
      if (ship instanceof PlayerShip) {
        Laser.lastShotData.gunId = gunId;
        Laser.lastShotData.withSound = i == 0;
        GameScreen.enemiesProcessor.shot(Laser.lastShotData);
      }
      LasersRepository.addTurret(l);
    }
    if (newShotTime - lastSoundPLayTime >= definition.soundPlayInterval) {
      playShotSound(soundScale, pan);
      lastSoundPLayTime = newShotTime;
    }
    this.lastShotTime = newShotTime;
  }
}
