package com.mikilangelo.abysmal.screens.menu.options;

import com.mikilangelo.abysmal.screens.menu.MenuScreen;

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
