package com.mikilangelo.abysmal.screens.game.uiElements;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;

public abstract class AbilityButton extends Button {

  private final Sprite icon;
  private final Sprite background;
  private final Texture border;
  private final float reloadTimeS;
  private final byte maxTextLength;
  private float secondsLeft;
  private GlyphLayout textBox;

  public AbilityButton(
          Sprite icon, Sprite background, Texture border,
          float reloadTimeS, float x, float y, float radius) {
    this.icon = icon;
    this.background = background;
    this.border = border;
    this.reloadTimeS = reloadTimeS;
    this.maxTextLength = (byte) String.valueOf(MathUtils.ceil(reloadTimeS)).length();
    resize(x, y, radius);
  }

  @Override
  public void draw(Batch batch) {
    if (secondsLeft > 0) {
      icon.setAlpha(0.4f);
      icon.draw(batch);
      drawArc(batch, 1 - secondsLeft / reloadTimeS);
    } else {
      batch.draw(icon, x - radius, y - radius, radius * 2, radius * 2);
    }
  }

  @Override
  public void update(float secondsLeft) {
    this.secondsLeft = secondsLeft;
  }

  public void draw(Batch batch, BitmapFont font) {
    if (secondsLeft > 0) {
      secondsLeft = Math.min(secondsLeft, reloadTimeS);
      background.setAlpha(0.3f);
      background.draw(batch);
      font.setColor(0.9f, 0.9f, 0.9f, 1);
      final float fontSize = radius / maxTextLength;
      String currentText = String.valueOf(MathUtils.ceil(secondsLeft));
      currentText = "00000".substring(0, maxTextLength - currentText.length()) + currentText;
      font.getData().setScale(fontSize * font.getScaleY() / font.getCapHeight());
      // font.draw(batch, currentText, x, y - fontSize * 0.5f);
      textBox = new GlyphLayout(font, currentText);
      font.draw(batch, textBox, x - textBox.width * 0.5f, y + textBox.height * 0.5f);
      drawArc(batch, 1 - secondsLeft / (float) reloadTimeS);
    } else {
      batch.draw(icon, x - radius, y - radius, radius * 2, radius * 2);
    }
  }

  protected void drawArc(Batch batch, float part) {
    final float step = 6.2832f * 0.011f;
    final float total = 6.2832f * part;
    final float pixelSize = RATIO * 1.2f;
    final float halfPixelSize = pixelSize * 0.5f;
    final float drawRadius = radius - pixelSize * 1.75f;
    for (float i = 0; i < total; i += step) {
      batch.draw(border,
              x + drawRadius * MathUtils.cos(i - 1.57f) - halfPixelSize,
              y - drawRadius * MathUtils.sin(i - 1.57f) - halfPixelSize,
              pixelSize, pixelSize);
    }
    batch.draw(border,
            x + drawRadius * MathUtils.cos(total - 1.57f) - halfPixelSize,
            y - drawRadius * MathUtils.sin(total - 1.57f) - halfPixelSize,
            pixelSize, pixelSize);
  }

  protected void resize(float x, float y, float radius) {
    this.x = x;
    this.y = y;
    this.radius = radius;
    icon.setCenter(x, y);
    icon.setScale(radius * 2 / icon.getHeight());
    background.setCenter(x, y);
    background.setScale(radius * 2 / background.getHeight());
  }

}
