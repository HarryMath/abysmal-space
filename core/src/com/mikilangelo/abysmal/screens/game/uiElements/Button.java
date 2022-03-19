package com.mikilangelo.abysmal.screens.game.uiElements;

import com.mikilangelo.abysmal.shared.tools.CalculateUtils;

public abstract class Button extends InterfaceElement {
  protected float x = 0;
  protected float y = 0;
  protected float radius = 0;

  public boolean contains(float touchX, float touchY) {
    if (Math.abs(touchX - x) < radius * 1.1f && Math.abs(touchY - y) < radius * 1.1f) {
      return CalculateUtils.distance(touchX, touchY, x, y) < radius * 1.1f;
    }
    return false;
  }
}
