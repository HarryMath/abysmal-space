package com.mikilangelo.abysmal.ui;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mikilangelo.abysmal.components.repositories.SoundsRepository;
import com.mikilangelo.abysmal.components.repositories.TexturesRepository;
import com.mikilangelo.abysmal.tools.Geometry;
import com.mikilangelo.abysmal.ui.screens.GameScreen;

public class Radar {
  public int radius;
  private final Texture circle = TexturesRepository.get("UI/radar.png");
  private final Texture center = TexturesRepository.get("UI/radar_center.png");
  private final Sprite scanner = new Sprite(TexturesRepository.get("UI/radar_scanner.png"));
  private final Texture enemyTexture = TexturesRepository.get("UI/radar_enem.png");
  private final Texture asteroidTexture = TexturesRepository.get("UI/radar_asteroid.png");
  Music sound = SoundsRepository.getMusic("sounds/radar.mp3");
  public float x;
  public float y;
  public float angle = 90;
  public float detectRadius;

  public Radar(float detectRadius) {
    handleResize(1);
    this.detectRadius = detectRadius;
    sound.setLooping(true);
    sound.setVolume(0.01f);
    sound.play();
  }

  public void draw(Batch batch) {
    batch.draw(this.circle, this.x - radius, this.y - radius, radius * 2, radius * 2);
    this.scanner.setRotation(angle);
    this.scanner.draw(batch);
    batch.draw(this.center, this.x - radius, this.y - radius, radius * 2, radius * 2);

  }

  public void drawEnemy(Batch batch, float playerX, float playerY, float x, float y) {
    if (Geometry.distance(x, y, playerX, playerY) < detectRadius * 0.95f) {
      batch.draw(this.enemyTexture,
              this.x - radius * 1.5f + (x - playerX) / detectRadius * radius,
              this.y - radius * 1.5f + (y - playerY) / detectRadius * radius,
              radius * 3, radius * 3);
    }
  }

  public void drawAsteroid(Batch batch, float playerX, float playerY, float x, float y) {
    if (Geometry.distance(x, y, playerX, playerY) < detectRadius * 0.95f) {
      batch.draw(this.asteroidTexture,
              this.x - radius * 1.5f + (x - playerX) / detectRadius * radius,
              this.y - radius * 1.5f + (y - playerY) / detectRadius * radius,
              radius * 3, radius * 3);
    }
  }

  public void move() {
    this.angle += 2f;
    this.angle %= 360;
  }

  public void handleResize(float resizeCoefficient) {
    radius = GameScreen.HEIGHT / 9;
    this.x = GameScreen.WIDTH - GameScreen.HEIGHT * 0.04f - radius;
    this.y = GameScreen.HEIGHT * 0.96f - radius;
    this.scanner.setCenter(x, y);
    this.scanner.setScale(GameScreen.HEIGHT  / 4.5f / scanner.getHeight());
  }

  public void dispose() {
    sound.stop();
  }
}
