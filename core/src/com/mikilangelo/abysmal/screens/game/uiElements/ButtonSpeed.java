package com.mikilangelo.abysmal.screens.game.uiElements;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;

public class ButtonSpeed extends AbilityButton {

  public ButtonSpeed(float reloadTimeMs, float w, float h) {
    super(new Sprite(TexturesRepository.get("UI/buttons/speed.png")),
            new Sprite(TexturesRepository.get("UI/buttons/speedBack.png")),
            TexturesRepository.get("UI/buttons/speedBorder.png"),
            reloadTimeMs,
            w - SPEED_BUTTON_CENTER_X * RATIO,
            SPEED_BUTTON_CENTER_Y * RATIO,
            BUTTON_RADIUS * RATIO);
  }

  @Override
  public void handleScreenResize(float w, float h) {
    this.x = w - SPEED_BUTTON_CENTER_X * RATIO;
    this.y = SPEED_BUTTON_CENTER_Y * RATIO;
    this.radius = BUTTON_RADIUS * RATIO;
    this.resize(x, y, radius);
  }
}
