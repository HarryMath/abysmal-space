package com.mikilangelo.abysmal.ui.screens.options;

import com.mikilangelo.abysmal.ui.screens.MenuScreen;

public class SingleplayerOption implements Option {
  @Override
  public String getText() {
    return "Singleplayer";
  }

  @Override
  public void handleClick(MenuScreen screen) {
    screen.setSingleplayer();
  }
}
