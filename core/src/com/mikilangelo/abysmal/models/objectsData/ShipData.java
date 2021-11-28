package com.mikilangelo.abysmal.models.objectsData;

public class ShipData implements DestroyableObjectData, IdentityData {
  public String id;
  public float health;

  @Override
  public float getHealth() {
    return health;
  }

  @Override
  public void damage(float damage) {
    this.health -= damage;
  }

  @Override
  public String getId() {
    return id;
  }
}
