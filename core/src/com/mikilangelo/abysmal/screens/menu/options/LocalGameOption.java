package com.mikilangelo.abysmal.screens.menu.options;

import com.mikilangelo.abysmal.screens.menu.MenuScreen;

public class LocalGameOption extends BasicOption {

  private String text = "By local network";

  public LocalGameOption() {}

  public LocalGameOption(String text) {
    this.text = text;
  }

  @Override
  public String getText() {
    return text;
  }

  @Override
  public void handleClick(MenuScreen screen) {
    screen.setLocalMultiPlayer();
  }
}
