package com.mikilangelo.abysmal.screens.game.actors.ship;

import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_WIDTH;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mikilangelo.abysmal.shared.Settings;
import com.mikilangelo.abysmal.shared.defenitions.ShipDef;

public class ShipBee extends PlayerShip {

  private final float beePower;

  public ShipBee(ShipDef def, float x, float y) {
    super(def, x, y);
    beePower = def.speedPower * 0.013f;
  }

  @Override
  public void control(float direction, float power, float delta) {
    if (delta < 0) {
      simpleControl(direction); // for desktop
      return;
    }
    if (power > 0.8f || power < -0.8f) {
      power = power * speedCoefficient;
    }
    final Vector2 pos = primaryBody.getPosition();
    primaryBody.applyLinearImpulse(
            MathUtils.cos(direction) * power * beePower,
            MathUtils.sin(direction) * power * beePower,
            pos.x, pos.y, true
    );
    velocity = primaryBody.getLinearVelocity();
    primaryBody.setLinearVelocity(
            velocity.x * controlSpeedResistance,
            velocity.y * controlSpeedResistance);
  }

  private void simpleControl(float direction) {
    if (direction < 0 || direction > MathUtils.PI2) {
      return;
    }
    newAngle = direction;
    final float rotationLeft = (angle - newAngle + MathUtils.PI2) % MathUtils.PI2;
    final float rotationRight = (newAngle - angle + MathUtils.PI2) % MathUtils.PI2;
    final boolean isLeft = rotationLeft < rotationRight;
    float rotation = isLeft ? rotationLeft : rotationRight;

    rotation = rotation > 1f ? 1 : (rotation * 0.7f + 0.3f);
    if (angle != newAngle) {
      this.simpleRotate(isLeft ? -rotation : rotation);
      if (angle <= newAngle + def.controlPower * 0.15f && angle >= newAngle - def.controlPower * 0.15f) {
        this.body.setAngularVelocity(this.body.getAngularVelocity() * 0.9f);
      }
    }
  }

  private void simpleRotate(float direction) {
    isUnderControl = true;
    this.body.setAngularVelocity(this.body.getAngularVelocity() + direction * def.controlPower * 0.99f);
  }

  @Override
  public void handleRotate(float power, float delta) {
    if (Settings.cameraRotation) {
      rotate(power, delta);
      return;
    }
    if (power > 0.8f || power < -0.8f) {
      power = power * speedCoefficient;
    }
    final Vector2 pos = primaryBody.getPosition();
    final float a = power > 0 ? angle + 1.57f : angle - 1.57f;
    power = Math.abs(power);
    primaryBody.applyLinearImpulse(
            MathUtils.cos(a) * power * beePower,
            MathUtils.sin(a) * power * beePower,
            pos.x, pos.y, true
    );
    velocity = primaryBody.getLinearVelocity();
    primaryBody.setLinearVelocity(
            velocity.x * controlSpeedResistance,
            velocity.y * controlSpeedResistance);
  }

  @Override
  public void handleStop() {
    applyImpulse(-1f, false);
  }

  @Override
  public void applyImpulse(float power, boolean withParticles) {
    if (power > 0.8f || power < -0.8f) {
      power = power * speedCoefficient;
    }
    this.currentPower = (this.currentPower * 0.99f + power * 0.01f);
    isPowerApplied = isUnderControl = true;
    final Vector2 pos = primaryBody.getPosition();
    primaryBody.applyLinearImpulse(
            power * MathUtils.cos(angle) * beePower,
            power * MathUtils.sin(angle) * beePower,
            pos.x,
            pos.y,
            true);
    velocity = primaryBody.getLinearVelocity();
    primaryBody.setLinearVelocity(
            velocity.x * controlSpeedResistance,
            velocity.y * controlSpeedResistance);
    if (withParticles) kak();
  }

  @Override
  public void shotDirectly() {
    super.shotDirectly();
  }

  @Override
  public void shotByTurrets() {
    if (Math.abs(angle - newAngle) < 0.2f) {
      super.shotDirectly();
    }
  }
}
