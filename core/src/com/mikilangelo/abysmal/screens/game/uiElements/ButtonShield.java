package com.mikilangelo.abysmal.screens.game.uiElements;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;

public class ButtonShield extends ButtonWithReload {

  private float timeLeft;

  public ButtonShield(float reloadTimeMs, float w, float h) {
    super(new Sprite(TexturesRepository.get("UI/shield.png")),
            TexturesRepository.get("UI/indicators/red.png"),
            reloadTimeMs,
            w - SHIELD_BUTTON_CENTER_X * RATIO,
            SHIELD_BUTTON_CENTER_Y * RATIO,
            BUTTON_RADIUS * RATIO);
  }

  public void process(float timeLeft) {
    this.timeLeft = timeLeft;
  }

  public void draw(Batch batch) {
    super.draw(batch, timeLeft);
  }

  public void handleScreenResize(float w, float h) {
    this.x = w - SHIELD_BUTTON_CENTER_X * RATIO;
    this.y = SHIELD_BUTTON_CENTER_Y * RATIO;
    this.radius = BUTTON_RADIUS * RATIO;
    this.resize(x, y, radius);
  }
}
