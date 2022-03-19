package com.mikilangelo.abysmal.screens.game.uiElements;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mikilangelo.abysmal.shared.tools.CalculateUtils;

public class ButtonWithReload extends Button {

  private final Sprite icon;
  private final Sprite border;
  private final int reloadTime;
  private final byte maxTextLength;

  public ButtonWithReload(Sprite icon, Sprite border, int reloadTime, float x, float y, float radius) {
    this.icon = icon;
    this.border = border;
    this.reloadTime = reloadTime;
    this.maxTextLength = (byte) String.valueOf(reloadTime).length();
    resize(x, y, radius);
  }

  public void draw(Batch batch, BitmapFont font, int secondsLeft) {
    if (secondsLeft > 0) {
      secondsLeft = Math.min(secondsLeft, reloadTime);
      border.setAlpha(0.5f);
      border.draw(batch);
      font.setColor(1, 1, 1, 0.5f);
      final float fontSize = radius * 0.9f / maxTextLength;
      String currentText = String.valueOf(secondsLeft);
      currentText = "00000".substring(0, maxTextLength - currentText.length()) + currentText;
      font.getData().setScale(fontSize * font.getScaleY() / font.getCapHeight());
      font.draw(batch, currentText, x, y - fontSize * 0.5f);
      // TODO draw progress arc
    } else {
      border.setAlpha(1);
      border.draw(batch);
      icon.draw(batch);
    }
  }

  public void resize(float x, float y, float radius) {
    this.x = x;
    this.y = y;
    this.radius = radius;
    icon.setCenter(x, y);
    border.setCenter(x, y);
    icon.setScale(radius * 2 / icon.getHeight());
    border.setScale(radius * 2 / icon.getHeight());
  }
}
