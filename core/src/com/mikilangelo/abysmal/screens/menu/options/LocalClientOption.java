package com.mikilangelo.abysmal.screens.menu.options;

import com.mikilangelo.abysmal.screens.menu.MenuScreen;

public class LocalClientOption extends BasicOption {
  @Override
  public String getText() {
    return "Connect to server";
  }

  @Override
  public void handleClick(MenuScreen screen) {
    screen.setLocalClient();
  }
}
