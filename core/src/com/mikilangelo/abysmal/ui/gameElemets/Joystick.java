package com.mikilangelo.abysmal.ui.gameElemets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.mikilangelo.abysmal.tools.Geometry;

public class Joystick {
  private int radius;
  private final Texture circle = new Texture("UI/circle.png");
  private final Texture curJoystick = new Texture("UI/controller.png");
  private float startX = 70;
  private float startY = 70;
  private float touchX, touchY;
  private float directionAngle = 0;
  private float power = 0;
  private boolean controlled = false;

  public Joystick(int r) {
    radius = r;
  }

  public float getDirection() {
    return directionAngle;
  }

  public float getPower() {
    return power > radius ? 1 : power / radius;
  }

  public void startTouch(float touchX, float touchY) {
    this.startX = touchX;
    this.startY = touchY;
    this.touchX = this.touchY = 0;
    controlled = true;
  }

  public void update(float touchX, float touchY) {
    this.touchX = touchX;
    this.touchY = touchY;
    power = Geometry.distance(startX, startY, touchX, touchY);
    if (power > 0) {
      directionAngle = Geometry.simpleDefineAngle((touchX - startX) / power, (touchY - startY) / power, directionAngle);
    }
    controlled = true;
  }

  public void draw(Batch batch) {
    if (!controlled) {
      return;
    }
    batch.draw(circle, this.startX - radius, this.startY - radius, radius * 2, radius * 2);
    if (power <= radius) {
      batch.draw(curJoystick, touchX - radius, touchY - radius, radius * 2, radius * 2);
    } else {
      batch.draw(curJoystick, this.startX - radius + radius * (touchX - this.startX) / power, this.startY - radius + radius * (touchY - this.startY) / power, radius * 2, radius * 2);
    }
    controlled = false;
  }

  public void handleResize(float resizeCoefficient) {
    radius *= resizeCoefficient;
    startX *= resizeCoefficient;
    startY *= resizeCoefficient;
  }

  public void dispose() {
    this.circle.dispose();
    this.curJoystick.dispose();
  }
}
