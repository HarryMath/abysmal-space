package com.mikilangelo.abysmal.screens.game.uiElements;

import com.badlogic.gdx.Gdx;

public abstract class InterfaceElement {

  protected static float RATIO; // created pixel size
  protected static final short BUTTON_RADIUS = 15;
  protected static final short JOYSTICK_CENTER_Y = BUTTON_RADIUS * 2 + 27;
  protected static final short JOYSTICK_CENTER_X = BUTTON_RADIUS * 2 + 33;

  protected static final short SHOT_BUTTON_CENTER_X = JOYSTICK_CENTER_X + BUTTON_RADIUS * 3 + 9;
  protected static final short SHOT_BUTTON_CENTER_Y = BUTTON_RADIUS + 15;

  protected static final short SHIELD_BUTTON_CENTER_X = JOYSTICK_CENTER_X + BUTTON_RADIUS * 4 + 9;
  protected static final short SHIELD_BUTTON_CENTER_Y = JOYSTICK_CENTER_Y;

  protected static final short SPEED_BUTTON_CENTER_X = JOYSTICK_CENTER_X + BUTTON_RADIUS * 3 + 9;
  protected static final short SPEED_BUTTON_CENTER_Y = JOYSTICK_CENTER_Y * 2 - SHOT_BUTTON_CENTER_Y;


  public static void handleResize(float h) {
    float pixelsPerCentimeter = Gdx.graphics.getDensity() * 0.3937f * 160;
    RATIO = (0.3f * pixelsPerCentimeter + 0.1f * h * 0.7f) / 30;
  }
}
