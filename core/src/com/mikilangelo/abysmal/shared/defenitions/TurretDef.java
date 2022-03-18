package com.mikilangelo.abysmal.shared.defenitions;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class TurretDef {
  public boolean isAutomatic;
  public Sprite texture;
  public float size;
  public float positionX;
  public float positionY;
  public LaserDef laserDefinition;
  public byte lasersAmount;
  public float lasersDistance;
  public float shotInterval;
  public float rotationSpeed;
  public float soundPlayInterval = 0;
}
