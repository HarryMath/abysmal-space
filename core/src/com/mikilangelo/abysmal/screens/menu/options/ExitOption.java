package com.mikilangelo.abysmal.screens.menu.options;

import com.mikilangelo.abysmal.shared.MusicPlayer;
import com.mikilangelo.abysmal.shared.ShipDefinitions;
import com.badlogic.gdx.Gdx;
import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;
import com.mikilangelo.abysmal.screens.menu.MenuScreen;

public class ExitOption implements Option {
  @Override
  public String getText() {
    return "Quit";
  }

  @Override
  public void handleClick(MenuScreen screen) {
    TexturesRepository.disposeAll();
    ShipDefinitions.disposeAll();
    MusicPlayer.dispose();
    Gdx.app.postRunnable(() -> {
      Gdx.app.exit();
      System.exit(0);
    });
  }
}
