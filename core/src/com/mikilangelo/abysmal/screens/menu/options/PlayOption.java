package com.mikilangelo.abysmal.screens.menu.options;

import com.mikilangelo.abysmal.screens.menu.MenuScreen;

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
