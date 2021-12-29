package com.mikilangelo.abysmal.ui.gameElemets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.mikilangelo.abysmal.components.repositories.TexturesRepository;
import com.mikilangelo.abysmal.tools.Geometry;
import com.mikilangelo.abysmal.ui.screens.GameScreen;

public class Radar extends InterfaceElement {
  private final Texture back = TexturesRepository.get("UI/radar/back.png");
  private final Sprite center = new Sprite(TexturesRepository.get("UI/radar/center.png"));
  private final Texture border = TexturesRepository.get("UI/radar/border.png");
  private final Texture overlay = TexturesRepository.get("UI/radar/overlay.png");
  private final Sprite enemy = new Sprite(TexturesRepository.get("UI/radar/enemy.png"));
  private final Sprite asteroid = new Sprite(TexturesRepository.get("UI/radar/asteroid.png"));
  private float x, y;
  private int drawRadius;
  private final float detectRadius;
  private float fullHeight;
  private float time = 0;

  private float dx, dy, distance;

  public Radar(float detectRadius, float screenHeight, float screenWidth) {
    this.detectRadius = detectRadius * 0.9f;
    resize(screenHeight, screenWidth);
  }

  public void draw(Batch batch) {
    batch.draw(this.back, this.x - drawRadius, this.y - drawRadius, fullHeight, fullHeight);
  }

  public void drawOverlay(Batch batch, float angle) {
    this.center.setRotation(angle * MathUtils.radiansToDegrees);
    this.center.draw(batch);
    time = (time + 0.2f) % 6;
    batch.draw(this.overlay, this.x - drawRadius,
            this.y - drawRadius - Math.round(time) * 0.5f * RATIO,
            fullHeight, fullHeight);
    batch.draw(this.border, this.x - drawRadius, this.y - drawRadius, fullHeight, fullHeight);
  }

  public void drawEnemy(Batch batch, float playerX, float playerY, float x, float y) {
    drawObject(batch, enemy, playerX, playerY, x, y);
  }

  public void drawAsteroid(Batch batch, float playerX, float playerY, float x, float y) {
    drawObject(batch, asteroid, playerX, playerY, x, y);
  }

  private void drawObject(Batch batch, Sprite sprite, float playerX, float playerY, float x, float y) {
    dx = Math.abs(playerX - x); dy = Math.abs(playerY - y);
    if (dx < detectRadius && dy < detectRadius) {
      sprite.setCenter(this.x + (x - playerX) / detectRadius * drawRadius * 0.92f,
              this.y + (y - playerY) / detectRadius * drawRadius * 0.92f);
      if (dx + dy > detectRadius * 0.2f) {
        distance = (dx * dx + dy * dy) / (detectRadius * detectRadius * 2);
        if (distance > 0.04f) {
          sprite.setAlpha(1.04167f * (0.04f - distance) + 1);
        } else {
          sprite.setAlpha(1);
        }
      } else {
        sprite.setAlpha(1);
      }
      sprite.draw(batch);
    }
  }

  public void resize(float h, float w) {
    fullHeight = 46 * RATIO;
    drawRadius = (int) (fullHeight * 0.5f);
    x = w - 6 * RATIO - drawRadius;
    y = h - 6 * RATIO - drawRadius;
    center.setCenter(x, y);
    center.setScale( RATIO * 2.5f / center.getHeight());
    asteroid.setScale( RATIO * 1.3f / asteroid.getHeight());
    enemy.setScale( RATIO * 1.42f / enemy.getHeight());
  }
}
