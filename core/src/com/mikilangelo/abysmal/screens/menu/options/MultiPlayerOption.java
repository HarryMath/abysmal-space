package com.mikilangelo.abysmal.screens.menu.options;

import com.mikilangelo.abysmal.screens.menu.MenuScreen;

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
