package com.mikilangelo.abysmal.screens.menu.options;

import com.mikilangelo.abysmal.screens.menu.MenuScreen;

public class GlobalGameOption extends BasicOption {
  @Override
  public String getText() {
    return "Online multiplayer";
  }

  @Override
  public void handleClick(MenuScreen screen) {
    screen.setGlobalMultiplayer();
  }
}
