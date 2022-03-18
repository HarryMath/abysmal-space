package com.mikilangelo.abysmal.shared.defenitions;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

public class LaserDef {

  public Sprite texture;
  public Array<Sprite> explosionTextures;
  public Sound sound;
  public float impulse;
  public float damage;
  public int touches;
  public float lifeTime;
  public float density;
}
