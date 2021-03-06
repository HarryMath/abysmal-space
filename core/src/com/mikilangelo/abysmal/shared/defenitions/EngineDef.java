package com.mikilangelo.abysmal.shared.defenitions;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class EngineDef {
  public Sprite particleTexture;
  public final float[] color = new float[] {1, 1, 1};
  public boolean isTopLayer = false;
  public boolean withTint = false;
  public float particlePositionDispersion;
  public float particleSpeedDispersion;
  public float decayRate;
  public float particleScale;
  public float particleSizeDispersion;
  public float particleShipSpeedCoefficient;
  public float positionX;
  public float positionY;
  public float initialParticleOpacity = 0.1f;
  public float particleAppearChance = 1;
  public float lightDecay = 0.07f;
  public boolean isResizing = false;
  public int srcBlendFunc = GL20.GL_SRC_ALPHA;
  public int distBlendFunc = GL20.GL_ONE_MINUS_SRC_ALPHA;
}
