package com.mikilangelo.abysmal.ui.gameElemets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mikilangelo.abysmal.components.repositories.TexturesRepository;

public class PartIndicator extends InterfaceElement {
  private final Sprite bar;
  private float width, height;
  private final float maxValue;
  private float shownValue = 0;
  private float x, y;

  public PartIndicator(String color, float maxValue, float maxWidth, float height, float x, float y) {
    bar = new Sprite(TexturesRepository.get("UI/indicators/" + color + ".png"));
    this.maxValue = maxValue;
    this.width = maxWidth;
    this.height = height;
    this.x = x;
    this.y = y;
  }

  public void draw(Batch batch, float currentValue) {
    if (currentValue < 0) {
      currentValue = 0;
    } else if (currentValue > maxValue) {
      currentValue = maxValue;
    }
    shownValue = shownValue * 0.95f + currentValue * 0.05f;
    bar.setAlpha(0.25f + 0.45f * shownValue / maxValue);
    bar.draw(batch);
    batch.draw(bar, x, y, width * shownValue / maxValue, height);
    batch.draw(bar, x + width * shownValue / maxValue - RATIO * 0.005f,
            y + height * 0.25f, RATIO * 0.9f, height * 0.5f);
  }

  public void resize(float w, float h, float x, float y) {
    width = w;
    height = h;
    this.x = x;
    this.y = y;
    bar.setCenter(x + width / 2, y + 2 * RATIO);
    bar.setScale(width + RATIO, 2.2f * RATIO);
  }
}
