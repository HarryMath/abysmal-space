package com.mikilangelo.abysmal.screens.game.uiElements;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mikilangelo.abysmal.shared.tools.CalculateUtils;

public class ActivationButton extends InterfaceElement {

  private final Sprite icon;
  private final Sprite border;
  private final int reloadTime;
  private float x, y, radius;

  public ActivationButton(Sprite icon, Sprite border, int reloadTime, float x, float y, float radius) {
    this.icon = icon;
    this.border = border;
    this.reloadTime = reloadTime;
    resize(x, y, radius);
  }

  public void draw(Batch batch, BitmapFont font, int secondsLeft) {
    if (secondsLeft > 0) {
      secondsLeft = Math.min(secondsLeft, reloadTime);
      border.setAlpha(0.5f);
      border.draw(batch);
      font.setColor(1, 1, 1, 0.5f);
      final byte maxLength = (byte) String.valueOf(reloadTime).length();
      final float fontSize = radius * 0.9f / maxLength;
      String currentText = String.valueOf(secondsLeft);
      currentText = "0000".substring(0, maxLength - currentText.length()) + currentText;
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

  public boolean contains(float touchX, float touchY) {
    if (Math.abs(touchX - x) < radius * 1.1f && Math.abs(touchY - y) < radius * 1.1f) {
      return CalculateUtils.distance(touchX, touchY, x, y) < radius * 1.1f;
    }
    return false;
  }
}
