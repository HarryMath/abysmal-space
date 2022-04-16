package com.mikilangelo.abysmal.screens.menu.options;

import com.mikilangelo.abysmal.screens.menu.MenuScreen;

public interface Option {

  String getText();

  void handleClick(MenuScreen screen);

  boolean isHovered();

  void setHovered(boolean isHovered);
}
