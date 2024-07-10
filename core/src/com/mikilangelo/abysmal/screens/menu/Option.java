package com.mikilangelo.abysmal.screens.menu;

import com.badlogic.gdx.audio.Sound;
import com.mikilangelo.abysmal.shared.repositories.SoundsRepository;

public class Option {

  final static Sound menuSound = SoundsRepository.getSound("sounds/menu_button.mp3");
  private boolean isHovered = false;

  protected final String text;
  protected final Runnable action;

  public Option(String text, Runnable action) {
    this.text = text;
    this.action = action;
  }

  public String getText() {
    return text;
  }

  public void click() {
    action.run();
  }

  public boolean isHovered() {
    return isHovered;
  }

  public void setHovered(boolean isHovered) {
    if (isHovered && !this.isHovered) {
      menuSound.play(0.4f);
    }
    this.isHovered = isHovered;
  }
}
