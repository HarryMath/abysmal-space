package com.mikilangelo.abysmal.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Circle;
import com.mikilangelo.abysmal.components.repositories.TexturesRepository;
import com.mikilangelo.abysmal.tools.Geometry;
import com.mikilangelo.abysmal.ui.screens.GameScreen;

public class Shooter {
  public boolean turretQ;
  public Circle area;
  private final Texture circle = TexturesRepository.get("UI/circle.png");
  private final Texture button = TexturesRepository.get("UI/shot.png");

  public Shooter(boolean turretQ) {
    handleResize(1);
    this.turretQ = turretQ;
  }

  public void draw(Batch batch, float x, float y) {
    batch.draw(circle, area.x - area.radius, area.y - area.radius, area.radius * 2, area.radius * 2);
    final float d = Geometry.distance(area.x, area.y, x, y);
    if (d <= area.radius) {
      batch.draw(button, x - area.radius, y - area.radius, area.radius * 2, area.radius * 2);
    } else {
      batch.draw(button, area.x - area.radius + area.radius * (x - area.x) / d, area.y - area.radius + area.radius * (y - area.y) / d, area.radius * 2, area.radius * 2);
    }
  }

  public void draw(Batch batch) {
    batch.draw(button, area.x - area.radius, area.y - area.radius, 2 * area.radius, 2 * area.radius);
  }

  public void handleResize(float coefficient) {
    area = new Circle(GameScreen.WIDTH - GameScreen.HEIGHT / 8, GameScreen.HEIGHT / 8, GameScreen.HEIGHT / 8);
  }
}
