package com.mikilangelo.abysmal.screens.game.uiElements;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;

public class ButtonShield extends AbilityButton {

  public ButtonShield(float reloadTimeS, float w, float h) {
    super(new Sprite(TexturesRepository.get("UI/buttons/shield.png")),
            new Sprite(TexturesRepository.get("UI/buttons/shieldBack.png")),
            TexturesRepository.get("UI/buttons/shieldBorder.png"),
            reloadTimeS,
            w - SHIELD_BUTTON_CENTER_X * RATIO,
            SHIELD_BUTTON_CENTER_Y * RATIO,
            BUTTON_RADIUS * RATIO);
  }

  @Override
  public void handleScreenResize(float w, float h) {
    this.x = w - SHIELD_BUTTON_CENTER_X * RATIO;
    this.y = SHIELD_BUTTON_CENTER_Y * RATIO;
    this.radius = BUTTON_RADIUS * RATIO;
    this.resize(x, y, radius);
  }
}
