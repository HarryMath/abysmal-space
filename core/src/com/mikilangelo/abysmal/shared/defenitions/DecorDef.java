package com.mikilangelo.abysmal.shared.defenitions;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class DecorDef {
  public final Sprite texture;
  public final boolean isLight;

  public DecorDef(Sprite texture, boolean isLight) {
    this.texture = texture;
    this.isLight = isLight;
  }
}
