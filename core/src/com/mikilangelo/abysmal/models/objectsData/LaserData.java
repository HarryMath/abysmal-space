package com.mikilangelo.abysmal.models.objectsData;

import com.badlogic.gdx.math.Vector2;


public class LaserData implements IdentityData {
  public String shipId;
  public float damage;
  public byte contactsCounter;
  public Vector2 collision;

  @Override
  public String getId() {
    return shipId;
  }
}
