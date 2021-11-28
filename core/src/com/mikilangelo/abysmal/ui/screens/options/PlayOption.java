package com.mikilangelo.abysmal.ui.screens.options;

import com.mikilangelo.abysmal.ui.screens.MenuScreen;

public class PlayOption implements Option {

  @Override
  public String getText() {
    return "Play";
  }

  @Override
  public void handleClick(MenuScreen screen) {
    screen.handlePlayClick();
  }
}
