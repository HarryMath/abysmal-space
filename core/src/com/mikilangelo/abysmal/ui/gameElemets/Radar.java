package com.mikilangelo.abysmal.ui.gameElemets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.mikilangelo.abysmal.components.repositories.TexturesRepository;
import com.mikilangelo.abysmal.ui.screens.GameScreen;

public class Radar extends InterfaceElement {
  private final Texture back = TexturesRepository.get("UI/radar/back.png");
  private final Sprite center = new Sprite(TexturesRepository.get("UI/radar/center.png"));
  private final Texture border = TexturesRepository.get("UI/radar/border.png");
  private final Texture overlay = TexturesRepository.get("UI/radar/overlay.png");
  private final Texture enemy = TexturesRepository.get("UI/radar/enemy.png");
  private final Texture asteroid = TexturesRepository.get("UI/radar/asteroid.png");
  private float x, y;
  private int drawRadius;
  private final float detectRadius;
  private float fullHeight;
  private float time = 0;

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
    if (Math.abs(playerX - x) < detectRadius && Math.abs(playerY - y) < detectRadius) {
      batch.draw(this.enemy,
              this.x + (x - playerX) / detectRadius * drawRadius * 0.9f,
              this.y + (y - playerY) / detectRadius * drawRadius * 0.9f,
              RATIO * 1.5f, RATIO * 1.5f);
    }
  }

  public void drawAsteroid(Batch batch, float playerX, float playerY, float x, float y) {
    if (Math.abs(playerX - x) < detectRadius && Math.abs(playerY - y) < detectRadius) {
      batch.draw(this.asteroid,
              this.x + (x - playerX) / detectRadius * drawRadius * 0.9f,
              this.y + (y - playerY) / detectRadius * drawRadius * 0.9f,
              RATIO * 1.3f, RATIO * 1.3f);
    }
  }

  public void resize(float h, float w) {
    fullHeight = 46 * RATIO;
    drawRadius = (int) (fullHeight * 0.5f);
    this.x = w - 6 * RATIO - drawRadius;
    this.y = h - 6 * RATIO - drawRadius;
    this.center.setCenter(x, y);
    this.center.setScale( 2.5f * RATIO / center.getHeight());
  }
}
