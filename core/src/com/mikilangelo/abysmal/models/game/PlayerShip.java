package com.mikilangelo.abysmal.models.game;

import com.mikilangelo.abysmal.models.definitions.ShipDef;

public class PlayerShip extends Ship {

  public static float X = 0;
  public static float Y = 0;

  public PlayerShip(ShipDef def, float x, float y) {
    super(def, x, y, false, x, y);
  }

  @Override
  public void move(float delta) {
    super.move(delta);
    X = this.x;
    Y = this.y;
  }

  @Override
  public void shot() {
    this.shot(1, 0);
  }
}
