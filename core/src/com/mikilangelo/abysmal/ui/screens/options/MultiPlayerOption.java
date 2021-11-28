package com.mikilangelo.abysmal.ui.screens.options;

import com.mikilangelo.abysmal.ui.screens.MenuScreen;

public class MultiPlayerOption implements Option {
  @Override
  public String getText() {
    return "Multiplayer";
  }

  @Override
  public void handleClick(MenuScreen screen) {
    screen.setMultiplayer();
  }
}
