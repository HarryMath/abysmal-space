package com.mikilangelo.abysmal.ui.screens.options;

import com.mikilangelo.abysmal.ui.screens.MenuScreen;

public interface Option {

  String getText();

  void handleClick(MenuScreen screen);

}
