package com.mikilangelo.abysmal.ui.screens.options;

import com.mikilangelo.abysmal.ui.screens.MenuScreen;

public class MainMenuOption implements Option {
  @Override
  public String getText() {
    return "< Back";
  }

  @Override
  public void handleClick(MenuScreen screen) {
    screen.handleMainMenuOption();
  }
}
