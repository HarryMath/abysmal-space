package com.mikilangelo.abysmal.screens.game.uiElements;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.mikilangelo.abysmal.screens.game.uiElements.InterfaceElement;
import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;
import com.mikilangelo.abysmal.shared.tools.CalculateUtils;

public class JoystickShooter extends InterfaceElement {
  private final Texture circle = TexturesRepository.get("UI/circle.png");
  private final Texture button = TexturesRepository.get("UI/shot.png");
  public final boolean isActive;

  private float radius;
  private float centerX = 70;
  private float centerY = 70;
  private float touchX, touchY;
  private float directionAngle = 0;
  private float power = 0;
  private boolean controlled = false;

  public JoystickShooter(float w, float h, boolean isActive) {
    this.isActive = isActive;
    handleScreenResize(w, h);
  }

  public float getDirection() {
    return directionAngle;
  }

  public void startTouch(float touchX, float touchY) {
    this.centerX = touchX;
    this.centerY = touchY;
    this.touchX = this.touchY = 0;
    controlled = true;
  }

  public void update(float touchX, float touchY) {
    this.touchX = touchX;
    this.touchY = touchY;
    power = CalculateUtils.distance(centerX, centerY, touchX, touchY);
    if (power > 0) {
      directionAngle = CalculateUtils.simpleDefineAngle((touchX - centerX) / power, (touchY - centerY) / power, directionAngle);
    }
    controlled = true;
  }

  public void handleScreenResize(float w, float h) {
    centerX = w - JOYSTICK_CENTER_X * RATIO;
    centerY = JOYSTICK_CENTER_Y * RATIO;
    radius = BUTTON_RADIUS * RATIO * 2;
  }

  public void draw(Batch batch) {
    if (!isActive) return;
    if (!controlled) {
      return;
    }
    batch.draw(circle, centerX - radius, centerY - radius, radius * 2, radius * 2);
    if (power <= radius) {
      batch.draw(button, touchX - radius, touchY - radius, radius * 2, radius * 2);
    } else {
      batch.draw(button, centerX - radius + radius * (touchX - centerX) / power, centerY - radius + radius * (touchY - centerY) / power, radius * 2, radius * 2);
    }
    controlled = false;
  }
}
