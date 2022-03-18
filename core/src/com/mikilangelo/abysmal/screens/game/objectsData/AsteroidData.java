package com.mikilangelo.abysmal.screens.game.objectsData;

public class AsteroidData implements DestroyableObjectData {
  public float health;

  @Override
  public float getHealth() {
    return health;
  }

  @Override
  public void damage(float damage) {
    this.health -= damage;
  }
}
