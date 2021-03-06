package com.mikilangelo.abysmal.shared.defenitions;

import com.mikilangelo.abysmal.shared.tools.BodLLoader;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

public class ShipDef {

  public boolean isBee = false;

  // basic
  public String name;
  public int id;
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
  public DecorDef decorOver = null;
  public Sprite decorUnder = null;
  public boolean decorOnSpeed = true;
  public Array<Sprite> engineAnimation;
  public float frameFrequency;

  // lasers
  public LaserDef laserDefinition;
  public byte lasersAmount;
  public float lasersDistance;
  public int shotIntervalMs;
  public int ammo;

  // turrets
  public Array<TurretDef> turretDefinitions;

  // particles
  public Array<EngineDef> engineDefinitions;

  // shield
  public int shieldRechargeTimeMs = 45000; // ms
  public int shieldLifeTimeS = 15; // s
  public float laserX = 0;

  // speedUp
  public long speedRechargeTimeMs = 35000;
  public float speedTimeS = 10;
  public float speedUpCoefficient = 2;

  public void resizeTextures(float coefficient) {
    final float scale = size / bodyTexture.getHeight() * coefficient;
    bodyTexture.setScale(scale);
    if (decorUnder != null) {
      decorUnder.setScale(scale);
    }
    if (decorOver != null) {
      decorOver.texture.setScale(scale);
    }
    for (Sprite s: engineAnimation) {
      s.setScale(scale);
    }
  }
}
