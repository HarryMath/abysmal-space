package com.mikilangelo.abysmal.screens.game.uiElements;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;

public class ButtonShot extends Button {
  private final Texture texture = TexturesRepository.get("UI/buttons/fire.png");
  private final boolean isActive;

  public ButtonShot(float w, float h, boolean isActive) {
    this.isActive = isActive;
    this.handleScreenResize(w, h);
  }

  public void draw(Batch batch) {
    if (!isActive) return;
    batch.draw(texture, x - radius, y - radius, radius * 2, radius * 2);
  }

  @Override
  public boolean contains(float touchX, float touchY) {
    return isActive && super.contains(touchX, touchY);
  }

  public void handleScreenResize(float w, float h) {
    if (!isActive) return;
    this.x = w - SHOT_BUTTON_CENTER_X * RATIO;
    this.y = SHOT_BUTTON_CENTER_Y * RATIO;
    this.radius = BUTTON_RADIUS * RATIO;
  }
}
