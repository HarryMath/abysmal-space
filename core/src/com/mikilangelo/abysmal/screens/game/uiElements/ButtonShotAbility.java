package com.mikilangelo.abysmal.screens.game.uiElements;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;

public class ButtonShotAbility extends AbilityButton {

  public ButtonShotAbility(float w, float h, float reloadTimeS) {
    super(new Sprite(TexturesRepository.get("UI/buttons/fire.png")),
            new Sprite(TexturesRepository.get("UI/buttons/fireBack.png")),
            TexturesRepository.get("UI/buttons/fireBorder.png"),
            reloadTimeS,
            w - SHIELD_BUTTON_CENTER_X * RATIO,
            SHIELD_BUTTON_CENTER_Y * RATIO,
            BUTTON_RADIUS * RATIO);
    this.handleScreenResize(w, h);
  }

  public void handleScreenResize(float w, float h) {
    this.x = w - SHOT_BUTTON_CENTER_X * RATIO;
    this.y = SHOT_BUTTON_CENTER_Y * RATIO;
    this.radius = BUTTON_RADIUS * RATIO;
    resize(x, y, radius);
  }
}
