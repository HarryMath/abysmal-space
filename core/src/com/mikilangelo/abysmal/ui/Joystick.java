package com.mikilangelo.abysmal.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.mikilangelo.abysmal.tools.Geometry;

public class Joystick {
  public static int radius;
  private final Texture circle = new Texture("UI/circle.png");
  private final Texture curJoystick = new Texture("UI/controller.png");
  public float x = 70;
  public float y = 70;

  public Joystick(int radius) {
    this.radius = radius;
  }

  public void draw(Batch batch, float x, float y) {
    batch.draw(circle, this.x - radius, this.y - radius, radius * 2, radius * 2);
    float d = Geometry.distance(x, y, this.x, this.y);
    if (d <= radius) {
      batch.draw(curJoystick, x - radius, y - radius, radius * 2, radius * 2);
    } else {
      batch.draw(curJoystick, this.x - radius + radius * (x - this.x) / d, this.y - radius + radius * (y - this.y) / d, radius * 2, radius * 2);
    }
  }

  public void handleResize(float resizeCoefficient) {
    radius *= resizeCoefficient;
    x *= resizeCoefficient;
    y *= resizeCoefficient;
  }

  public void dispose() {
    this.circle.dispose();
    this.curJoystick.dispose();
  }
}
