package com.mikilangelo.abysmal.screens.game.uiElements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public class ButtonWithReload extends Button {

  private final Sprite icon;
  private final Texture border;
  private final float reloadTimeS;
  private final byte maxTextLength;

  public ButtonWithReload(
          Sprite icon, Texture border,
          float reloadTimeS, float x, float y, float radius) {
    this.icon = icon;
    this.border = border;
    this.reloadTimeS = reloadTimeS;
    this.maxTextLength = (byte) String.valueOf(reloadTimeS * 0.001).length();
    resize(x, y, radius);
  }

  public void draw(Batch batch, float sLeft) {
    if (sLeft > 0) {
      icon.setAlpha(0.1f);
      icon.draw(batch);
      drawArc(batch, 1 - sLeft / reloadTimeS);
    } else {
      icon.setAlpha(1f);
      icon.draw(batch);
    }
  }

  public void draw(Batch batch, BitmapFont font, float secondsLeft) {
    if (secondsLeft > 0) {
      secondsLeft = Math.min(secondsLeft, reloadTimeS);
      icon.setAlpha(0.2f);
      icon.draw(batch);
      font.setColor(1, 1, 1, 0.5f);
      final float fontSize = radius * 0.9f / maxTextLength;
      String currentText = String.valueOf(secondsLeft);
      currentText = "00000".substring(0, maxTextLength - currentText.length()) + currentText;
      font.getData().setScale(fontSize * font.getScaleY() / font.getCapHeight());
      font.draw(batch, currentText, x, y - fontSize * 0.5f);
      drawArc(batch, 1 - secondsLeft / (float) reloadTimeS);
    } else {
      icon.setAlpha(1);
      icon.draw(batch);
    }
  }

  public void drawArc(Batch batch, float part) {
    final float step = 6.2832f * 0.01f;
    final float total = 6.2832f * part;
    final float pixelSize = RATIO;
    final float halfPixelSize = pixelSize * 0.5f;
    for (float i = 0; i < total; i += step) {
      batch.draw(border,
              x + radius * MathUtils.cos(i - 1.57f) - halfPixelSize,
              y - radius * MathUtils.sin(i - 1.57f) - halfPixelSize,
              pixelSize, pixelSize);
    }
    batch.draw(border,
            x + radius * MathUtils.cos(total - 1.57f) - halfPixelSize,
            y - radius * MathUtils.sin(total - 1.57f) - halfPixelSize,
            pixelSize, pixelSize);
  }

  public void resize(float x, float y, float radius) {
    this.x = x;
    this.y = y;
    this.radius = radius;
    icon.setCenter(x, y);
    icon.setScale(radius * 2 / icon.getHeight());
  }

}
