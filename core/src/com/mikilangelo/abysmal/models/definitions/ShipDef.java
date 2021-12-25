package com.mikilangelo.abysmal.models.definitions;

import com.mikilangelo.abysmal.tools.BodLLoader;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

public class ShipDef {

  // basic
  public String name;
  public float health;
  public float maxZoom;
  public float radarPower;

  // body
  public float size;
  public BodLLoader bodyLoader;
  public float bodyScale;
  public float density;
  public float friction;
  public float restitution;
  public float shieldRadius;

  // dynamic
  public float speedResistance;
  public float rotationResistance;
  public float controlResistanceOnSpeed;
  public float controlPower;
  public float speedPower;
  public float maxSpeed = 0;

  // texture
  public Sprite bodyTexture;
  public Sprite decor = null;
  public Array<Sprite> engineAnimation;
  public float frameFrequency;

  // lasers
  public LaserDef laserDefinition;
  public byte lasersAmount;
  public float lasersDistance;
  public float shotInterval;

  // turrets
  public Array<TurretDef> turretDefinitions;

  // particles
  public Array<EngineDef> engineDefinitions;

  // shield
  public int shieldRechargeTime = 45000; // ms
  public int shieldLifeTime = 15; // s
}
