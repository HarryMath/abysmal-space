package com.mikilangelo.abysmal.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mikilangelo.abysmal.components.repositories.TexturesRepository;

public class HealthIndicator {

  private float barHeight;
  private float barWidth;
  private float totalWidth;
  private float paddingX;
  private float paddingY;
  private final byte barsAmount;
  private final float totalHealth;
  private final float healthPerBar;

  private final Texture background = TexturesRepository.get("UI/health/center.png");
  private final Texture blizzard = TexturesRepository.get("UI/health/blizzard.png");
  private final Texture gray = TexturesRepository.get("UI/health/gray.png");
  private final Texture white = TexturesRepository.get("UI/health/white.png");
  private final Texture leftBar = TexturesRepository.get("UI/health/leftBar.png");
  private final Texture rightBar = TexturesRepository.get("UI/health/rightBar.png");
  private final Sprite full = new Sprite(TexturesRepository.get("UI/health/full.png"));

  public HealthIndicator(float totalHealth, int h) {
    this.totalHealth = totalHealth;
    barsAmount = (byte) Math.ceil(Math.sqrt(totalHealth) / 2);
    healthPerBar = totalHealth / barsAmount;
    resize(h);
  }

  public void draw(Batch batch, BitmapFont font, float currentHealth) {
    batch.draw(background, paddingX, paddingY, totalWidth, barHeight);
    batch.draw(leftBar, paddingX, paddingY, barWidth, barHeight);
    batch.draw(rightBar, totalWidth + paddingX - barWidth, paddingY, barWidth, barHeight);
    batch.draw(blizzard, paddingX + barWidth * 0.5f, paddingY, barWidth, barHeight);
    float healthLeft = currentHealth;
    for (byte i = 0; i < barsAmount; i++) {
      if (healthLeft > 0 && healthLeft <= healthPerBar) {
        batch.draw(white, paddingX + barWidth * (i + 2), paddingY, barWidth, barHeight);
        full.setAlpha(healthLeft/healthPerBar);
        full.setCenter(paddingX + barWidth * (i + 2.5f), paddingY + barHeight / 2f);
        healthLeft -= healthPerBar;
        full.draw(batch);
      } else {
        batch.draw(gray, paddingX + barWidth * (i + 2), paddingY, barWidth, barHeight);
        if (healthLeft > 0){
          batch.draw(full, paddingX + barWidth * (i + 2), paddingY, barWidth, barHeight);
          healthLeft -= healthPerBar;
        }
      }
    }
    font.setColor(1, 1, 1, 1);
    font.getData().setScale(barHeight * 0.6f / font.getLineHeight() * font.getScaleY());
    final GlyphLayout indicator = new GlyphLayout(font, Math.round(currentHealth / totalHealth * 100) + "%");
    font.draw(batch, indicator,
            paddingX + barWidth * (barsAmount + 4) - indicator.width * 0.5f,
            paddingY + barHeight * 0.5f + indicator.height * 0.5f);
  }

  public void resize(int h) {
    final float screenDensity = Gdx.graphics.getDensity();
    barHeight = Math.min((40 + h * 0.13f) / 3 * (float) Math.sqrt(screenDensity), h / 10f);
    barWidth = barHeight / 40 * 22;
    paddingX = barWidth / 2;
    paddingY = h - barHeight - paddingX;
    totalWidth = barWidth * (barsAmount + 6);
    full.setScale(barHeight / full.getHeight());
  }
}
