package com.mikilangelo.abysmal.screens.game.uiElements;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;
import com.mikilangelo.abysmal.screens.game.actors.ship.PlayerShip;

public class Radar extends InterfaceElement {
  private final Texture back = TexturesRepository.get("UI/radar/back.png");
  private final Texture speedIcon = TexturesRepository.get("UI/indicators/speed.png");
  private final Sprite center = new Sprite(TexturesRepository.get("UI/radar/center.png"));
  private final Texture border = TexturesRepository.get("UI/radar/border.png");
  private final Texture overlay = TexturesRepository.get("UI/radar/overlay.png");
  private final Sprite enemy = new Sprite(TexturesRepository.get("UI/radar/enemy.png"));
  private final Sprite asteroid = new Sprite(TexturesRepository.get("UI/radar/asteroid.png"));
  private float x, y;
  private int drawRadius;
  private final float detectRadius;
  private final PartIndicator speedIndicator;
  private final float maxSpeed;
  private float fullWidth, fullHeight;
  private float time = 0;

  private float dx, dy, distance;
  private int playerX;
  private int playerY;

  public Radar(float detectRadius, float maxSpeed, float screenHeight, float screenWidth) {
    this.detectRadius = detectRadius * 0.9f;
    this.maxSpeed = maxSpeed;
    speedIndicator = new PartIndicator("blue", maxSpeed, 0, 0, 0, 0);
    resize(screenHeight, screenWidth);
  }

  public void drawBack(Batch batch) {
    batch.draw(this.back, this.x - drawRadius, this.y - fullHeight + drawRadius, fullWidth, fullHeight);
  }

  public void draw(Batch batch, BitmapFont digitsFont, BitmapFont lettersFont, float angle) {
    playerX = (int) PlayerShip.X;
    playerY = (int) PlayerShip.Y;
    this.center.setRotation(angle * MathUtils.radiansToDegrees);
    this.center.draw(batch);
    time = (time + 0.2f) % 6;
    batch.draw(this.overlay, x - drawRadius,
            y - drawRadius - Math.round(time) * 0.5f * RATIO,
            fullWidth, fullWidth);
    speedIndicator.draw(batch, PlayerShip.SPEED);
    batch.draw(this.border, x - drawRadius, y - fullHeight + drawRadius, fullWidth, fullHeight);
    digitsFont.getData().setScale(44 * RATIO / 9 / (digitsFont.getCapHeight() / digitsFont.getScaleY() * 4 / 5 ));
    lettersFont.getData().setScale(42 * RATIO / 9 / (lettersFont.getCapHeight() / lettersFont.getScaleY() * 4 / 5 ));
    byte length = (byte) String.valueOf(playerX).length();
    String zeros = " 000000".substring(0, 7 - length);
    float drawX = x - drawRadius + 2 * RATIO;
    float drawY = y + drawRadius - fullHeight - RATIO;
    drawX += drawWithShadow(batch, lettersFont, "X:", drawX, drawY, 0.8f);
    drawX += drawWithShadow(batch, digitsFont, zeros, drawX, drawY, 0.1f) + digitsFont.getCapHeight() / 5;
    drawWithShadow(batch, digitsFont, String.valueOf(playerX), drawX, drawY, 1f);
    drawX = x - drawRadius + 2 * RATIO;
    drawY -= digitsFont.getLineHeight() + RATIO * 0.5f;
    length = (byte) String.valueOf(playerY).length();
    zeros = " 000000".substring(0, 7 - length);
    drawX += drawWithShadow(batch, lettersFont, "Y:", drawX, drawY, 0.8f);
    drawX += drawWithShadow(batch, digitsFont, zeros, drawX, drawY, 0.1f) + digitsFont.getCapHeight() / 5;
    drawWithShadow(batch, digitsFont, String.valueOf(playerY), drawX, drawY, 1f);
  }

  private float drawWithShadow(Batch batch, BitmapFont font, String text, float x, float y, float a) {
    font.setColor(0, 0, 0,  0.2f + 0.7f * a);
    font.draw(batch, text, x + 0.5f * RATIO, y - 0.5f * RATIO);
    font.draw(batch, text, x - 0.5f * RATIO, y + 0.5f * RATIO);
    font.setColor(0.65f, 0.75f, 0.85f, a);
    return font.draw(batch, text, x, y).width;
  }

  public void drawEnemy(Batch batch, float x, float y) {
    drawObject(batch, enemy, playerX, playerY, x, y);
  }

  public void drawAsteroid(Batch batch, float x, float y) {
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
    fullWidth = 46 * RATIO;
    fullHeight = 50 * RATIO;
    drawRadius = (int) (fullWidth * 0.5f);
    x = w - 6 * RATIO - drawRadius;
    y = h - 6 * RATIO - drawRadius;
    speedIndicator.resize(fullWidth - 4 * RATIO, 2.1f * RATIO,
            w - 6 * RATIO - fullWidth + 2 * RATIO,
            h - 6 * RATIO - fullHeight + 2 * RATIO);
    center.setCenter(x, y);
    center.setScale( RATIO * 2.5f / center.getHeight());
    asteroid.setScale( RATIO * 1.3f / asteroid.getHeight());
    enemy.setScale( RATIO * 1.42f / enemy.getHeight());
  }
}
