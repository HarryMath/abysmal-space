package com.mikilangelo.abysmal.screens.game.uiElements;

import com.badlogic.gdx.Gdx;

public abstract class InterfaceElement {

  protected static float RATIO; // created pixel size
  protected static final short BUTTON_RADIUS = 15;
  protected static final short JOYSTICK_CENTER_X = BUTTON_RADIUS * 2 + 36;
  protected static final short JOYSTICK_CENTER_Y = BUTTON_RADIUS * 2 + 27;

  protected static final short SHOT_BUTTON_CENTER_X = JOYSTICK_CENTER_X + BUTTON_RADIUS * 3 + 1;
  protected static final short SHOT_BUTTON_CENTER_Y = BUTTON_RADIUS + 8;

  protected static final short SHIELD_BUTTON_CENTER_X = JOYSTICK_CENTER_X + BUTTON_RADIUS * 4 + 9;
  protected static final short SHIELD_BUTTON_CENTER_Y = JOYSTICK_CENTER_Y;

  protected static final short SPEED_BUTTON_CENTER_X = JOYSTICK_CENTER_X + BUTTON_RADIUS * 3 + 9;
  protected static final short SPEED_BUTTON_CENTER_Y = JOYSTICK_CENTER_Y * 2 - SHOT_BUTTON_CENTER_Y;


  public static void handleResize(float h) {
    float pixelsPerCentimeter = Gdx.graphics.getDensity() * 0.3937f * 160;
    RATIO = (
            0.4f * pixelsPerCentimeter +
            0.6f * h * 0.1f
    ) / 30;
  }
}
