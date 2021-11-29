package com.mikilangelo.abysmal.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Circle;
import com.mikilangelo.abysmal.components.repositories.TexturesRepository;
import com.mikilangelo.abysmal.tools.Geometry;
import com.mikilangelo.abysmal.ui.screens.GameScreen;

public class Shooter {
  public boolean turretQ;
  private final Texture circle = TexturesRepository.get("UI/circle.png");
  private final Texture button = TexturesRepository.get("UI/shot.png");

  private float radius;
  private float startX = 70;
  private float startY = 70;
  private float touchX, touchY;
  private float directionAngle = 0;
  private float power = 0;
  private boolean controlled = false;

  public Shooter(boolean turretQ) {
    handleResize(1);
    this.turretQ = turretQ;
  }

  public float getDirection() {
    return directionAngle;
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

  public boolean contains(float x, float y) {
    return Geometry.squaresSum(x - startX, y - startY) <= radius*radius;
  }

  public void handleResize(float resizeCoefficient) {
    startX = GameScreen.WIDTH - GameScreen.HEIGHT / 8;
    startY = GameScreen.HEIGHT / 8;
    radius = GameScreen.HEIGHT / 8;
  }

  public void draw(Batch batch) {
    if (controlled) {
      batch.draw(circle, startX - radius, startY - radius, radius * 2, radius * 2);
      if (power <= radius) {
        batch.draw(button, touchX - radius, touchY - radius, radius * 2, radius * 2);
      } else {
        batch.draw(button, startX - radius + radius * (touchX - startX) / power, startY - radius + radius * (touchY - startY) / power, radius * 2, radius * 2);
      }
    } else if (!turretQ){
      batch.draw(button, startX - radius, startY - radius, 2 * radius, 2 * radius);
    }
  }
}
