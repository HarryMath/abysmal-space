package com.mikilangelo.abysmal.screens.game.uiElements;

import com.badlogic.gdx.Gdx;

public abstract class InterfaceElement {

  protected static float RATIO;

  public static void handleResize(float h) {
    float pixelsPerCentimeter = Gdx.graphics.getDensity() * 0.3937f * 160;
    RATIO = (0.3f * pixelsPerCentimeter + 0.1f * h * 0.7f) / 30;
  }
}
