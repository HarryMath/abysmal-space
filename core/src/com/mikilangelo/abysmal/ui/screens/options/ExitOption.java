package com.mikilangelo.abysmal.ui.screens.options;

import com.mikilangelo.abysmal.components.ShipDefinitions;
import com.badlogic.gdx.Gdx;
import com.mikilangelo.abysmal.components.repositories.TexturesRepository;
import com.mikilangelo.abysmal.ui.screens.MenuScreen;

public class ExitOption implements Option {
  @Override
  public String getText() {
    return "Quit";
  }

  @Override
  public void handleClick(MenuScreen screen) {
    TexturesRepository.disposeAll();
    ShipDefinitions.disposeAll();
    Gdx.app.exit();
    System.exit(0);
  }
}
