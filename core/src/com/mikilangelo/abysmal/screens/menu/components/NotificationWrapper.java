package com.mikilangelo.abysmal.screens.menu.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class NotificationWrapper {
  private Notification notification;
  private boolean shown = false;
  private int w, h;

  public void draw(Batch batch, BitmapFont font) {
    if (!shown) return;
    notification.draw(batch, font);
  }

  public void showWarning(String text) {
    notification = new Notification(text, w, h);
    shown = true;
  }

  public void showInfo(String text) {
    notification = new Notification(text, w, h);
    shown = true;
  }

  public void hide() {
    shown = false;
  }

  public boolean isShown() {
    return shown;
  }

  public boolean isInButton(int x, int y) {
    return shown && notification.isInButton(x, y);
  }

  public void resize(int w, int h) {
    this.w = w;
    this.h = h;
    if (shown) {
      notification.resize(w, h);
    }
  }
}
