package com.mikilangelo.abysmal.shared.defenitions;

import com.mikilangelo.abysmal.shared.tools.BodLLoader;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

public class ShipDef {

  // basic
  public String name;
  public float health;
  public float maxZoom, minZoom = 0.5f;
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
  public float rotationResistance = 0.99f;
  public float rotationControlResistance;
  public float controlPower;
  public float speedPower;
  public float maxSpeed = 0;

  // texture
  public Sprite bodyTexture;
  public Sprite decorOver = null;
  public Sprite decorUnder = null;
  public Array<Sprite> engineAnimation;
  public float frameFrequency;

  // lasers
  public LaserDef laserDefinition;
  public byte lasersAmount;
  public float lasersDistance;
  public float shotInterval;
  public int ammo;

  // turrets
  public Array<TurretDef> turretDefinitions;

  // particles
  public Array<EngineDef> engineDefinitions;

  // shield
  public int shieldRechargeTime = 45000; // ms
  public int shieldLifeTime = 15; // s
  public float laserX = 0;

  public void resizeTextures(float coefficient) {
    final float scale = size / bodyTexture.getHeight() * coefficient;
    bodyTexture.setScale(scale);
    if (decorUnder != null) {
      decorUnder.setScale(scale);
    }
    if (decorOver != null) {
      decorOver.setScale(scale);
    }
    for (Sprite s: engineAnimation) {
      s.setScale(scale);
    }
  }
}
