package com.mikilangelo.abysmal.ui.screens.options;

import com.mikilangelo.abysmal.ui.screens.MenuScreen;

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
