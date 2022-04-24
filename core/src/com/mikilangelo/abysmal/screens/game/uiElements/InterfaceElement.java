package com.mikilangelo.abysmal.screens.game.uiElements;

import com.badlogic.gdx.Gdx;

public abstract class InterfaceElement {

  protected static float RATIO; // created pixel size
  protected static final short BUTTON_RADIUS = 14;
  protected static final short JOYSTICK_CENTER_X = BUTTON_RADIUS * 2 + 36;
  protected static final short JOYSTICK_CENTER_Y = BUTTON_RADIUS * 2 + 27;

  protected static final short SHOT_BUTTON_CENTER_X = 5 + JOYSTICK_CENTER_X + BUTTON_RADIUS * 3 + 1;
  protected static final short SHOT_BUTTON_CENTER_Y = BUTTON_RADIUS + 4;

  protected static final short SHIELD_BUTTON_CENTER_X = 5 + JOYSTICK_CENTER_X + BUTTON_RADIUS * 4 + 4;
  protected static final short SHIELD_BUTTON_CENTER_Y = JOYSTICK_CENTER_Y;

  protected static final short SPEED_BUTTON_CENTER_X = 5 + JOYSTICK_CENTER_X + BUTTON_RADIUS * 3 + 5;
  protected static final short SPEED_BUTTON_CENTER_Y = JOYSTICK_CENTER_Y * 2 - SHOT_BUTTON_CENTER_Y + 2;


  public static void handleResize(float h) {
    float pixelsPerCentimeter = Gdx.graphics.getDensity() * 0.3937f * 160;
    RATIO = (
            0.6f * pixelsPerCentimeter * 0.74f +
            0.4f * h * 0.1f
    ) / 30;
  }
}
