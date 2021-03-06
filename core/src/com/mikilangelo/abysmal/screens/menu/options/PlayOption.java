package com.mikilangelo.abysmal.screens.menu.options;

import com.mikilangelo.abysmal.screens.menu.MenuScreen;

public class PlayOption extends BasicOption {

  String text = "Play";

  public PlayOption() {}

  public PlayOption(String text) {
    this.text = text;
  }

  @Override
  public String getText() {
    return text;
  }

  @Override
  public void handleClick(MenuScreen screen) {
    screen.handlePlayClick();
  }
}
