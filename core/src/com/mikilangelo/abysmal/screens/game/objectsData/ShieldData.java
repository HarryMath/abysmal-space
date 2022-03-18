package com.mikilangelo.abysmal.screens.game.objectsData;

import com.badlogic.gdx.utils.Array;

public class ShieldData implements IdentityData {

  public String shipId;
  public boolean underPlayerFocus = false;
  public final Array<Touch> lastTouches = new Array<>(10);

  @Override
  public String getId() {
    return shipId;
  }

  @Override
  public void setPlayerFocus() {
    underPlayerFocus = true;
  }

  public void touch(float angle, float power) {
    this.lastTouches.add(new Touch(angle, power));
  }

  public class Touch {
    public final float angle;
    public final float power;

    public Touch(float angle, float power) {
      this.angle = angle;
      this.power = Math.min(power, 1);
    }
  }
}
