package com.mikilangelo.abysmal.models.objectsData;

import com.badlogic.gdx.utils.Array;

public class ShieldData implements IdentityData {

  public String shipId;
  public final Array<Float> lastTouches = new Array<>();

  @Override
  public String getId() {
    return shipId;
  }
}
