package com.mikilangelo.abysmal.screens.menu.options;

import com.mikilangelo.abysmal.screens.menu.MenuScreen;

public class LocalServerOption extends BasicOption {
  @Override
  public String getText() {
    return "Create Server";
  }

  @Override
  public void handleClick(MenuScreen screen) {
    screen.setLocalServer();
  }
}
