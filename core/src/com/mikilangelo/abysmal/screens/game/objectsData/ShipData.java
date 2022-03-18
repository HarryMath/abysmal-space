package com.mikilangelo.abysmal.screens.game.objectsData;

public class ShipData implements DestroyableObjectData, IdentityData {

  public String id;
  public boolean underPlayerFocus = false;
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

  @Override
  public void setPlayerFocus() {
    underPlayerFocus = true;
  }
}
