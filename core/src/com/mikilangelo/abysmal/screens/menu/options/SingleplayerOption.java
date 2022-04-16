package com.mikilangelo.abysmal.screens.menu.options;

import com.mikilangelo.abysmal.screens.menu.MenuScreen;

public class SingleplayerOption extends BasicOption {
  @Override
  public String getText() {
    return "Singleplayer";
  }

  @Override
  public void handleClick(MenuScreen screen) {
    screen.setSingleplayer();
  }
}
