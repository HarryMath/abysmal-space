package com.mikilangelo.abysmal.screens.menu.options;

import com.badlogic.gdx.audio.Sound;
import com.mikilangelo.abysmal.shared.repositories.SoundsRepository;

public abstract class BasicOption implements Option {

  final Sound menuSound = SoundsRepository.getSound("sounds/menu_button.mp3");

  boolean isHovered = false;

  @Override
  public boolean isHovered() {
    return isHovered;
  }

  @Override
  public void setHovered(boolean isHovered) {
    if (isHovered && !this.isHovered) {
      menuSound.play(0.4f);
    }
    this.isHovered = isHovered;
  }
}
