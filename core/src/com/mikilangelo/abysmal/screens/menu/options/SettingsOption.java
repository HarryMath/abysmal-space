package com.mikilangelo.abysmal.screens.menu.options;

import com.mikilangelo.abysmal.screens.menu.MenuScreen;

public class SettingsOption implements Option {
  @Override
  public String getText() {
    return "Settings";
  }

  @Override
  public void handleClick(MenuScreen screen) {
    screen.handleSettingsOption();
  }
}
