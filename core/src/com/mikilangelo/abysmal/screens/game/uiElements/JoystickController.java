package com.mikilangelo.abysmal.screens.game.uiElements;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;
import com.mikilangelo.abysmal.shared.tools.CalculateUtils;

public class JoystickController extends InterfaceElement {
  private float radius;
  private final Texture circle = TexturesRepository.get("UI/circle.png");
  private final Texture curJoystick = TexturesRepository.get("UI/controller.png");
  private float centerX = 70;
  private float centerY = 70;
  private float touchX, touchY;
  private float directionAngle = 1.5708f;
  private float power = 0;
  private boolean controlled = false;

  public JoystickController(float w, float h) {
    this.handleScreenResize(w, h);
  }

  public float getDirection() {
    return directionAngle;
  }

  public float getPower() {
    return power > radius ? 1 : power / radius;
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

  public void draw(Batch batch) {
    if (!controlled) {
      return;
    }
    batch.draw(circle, this.centerX - radius, this.centerY - radius, radius * 2, radius * 2);
    if (power <= radius) {
      batch.draw(curJoystick, touchX - radius, touchY - radius, radius * 2, radius * 2);
    } else {
      batch.draw(curJoystick, this.centerX - radius + radius * (touchX - this.centerX) / power, this.centerY - radius + radius * (touchY - this.centerY) / power, radius * 2, radius * 2);
    }
    controlled = false;
  }

  public void handleScreenResize(float w, float h) {
    radius = BUTTON_RADIUS * RATIO * 2.2f;
    centerX = JOYSTICK_CENTER_X * RATIO;
    centerY = JOYSTICK_CENTER_Y * RATIO;
  }

}
