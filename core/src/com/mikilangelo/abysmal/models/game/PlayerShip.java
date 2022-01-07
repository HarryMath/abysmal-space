package com.mikilangelo.abysmal.models.game;

import com.badlogic.gdx.math.Vector2;
import com.mikilangelo.abysmal.models.definitions.ShipDef;

public class PlayerShip extends Ship {

  public static float X = 0;
  public static float Y = 0;
  public static float SPEED = 0;
  private Vector2 velocity;

  public PlayerShip(ShipDef def, float x, float y) {
    super(def, x, y, false, x, y);
  }

  @Override
  public void move(float delta) {
    super.move(delta);
    X = this.x;
    Y = this.y;
    velocity = body.getLinearVelocity();
    SPEED = (float) Math.hypot(velocity.x, velocity.y);
  }

  @Override
  public void shot() {
    this.shot(1, 0);
  }
}
