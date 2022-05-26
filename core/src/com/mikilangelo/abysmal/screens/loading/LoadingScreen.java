package com.mikilangelo.abysmal.screens.loading;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.mikilangelo.abysmal.EnigmaSpace;
import com.mikilangelo.abysmal.shared.ShipDefinitions;
import com.mikilangelo.abysmal.shared.repositories.SoundsRepository;
import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;
import com.mikilangelo.abysmal.screens.game.actors.fixtures.Asteroid;
import com.mikilangelo.abysmal.screens.game.uiElements.Indicator;
import com.mikilangelo.abysmal.screens.menu.MenuScreen;

public class LoadingScreen implements Screen {

  final EnigmaSpace game;
  OrthographicCamera camera;

  public LoadingScreen(EnigmaSpace abysmalSpace) {
    this.game = abysmalSpace;
    camera = new OrthographicCamera();
    camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
    new Thread(() -> {
      SoundsRepository.init();
      Gdx.app.postRunnable(() -> {
        TexturesRepository.init();
        ShipDefinitions.init();
        Asteroid.init();
        Indicator.init();
        launchMenu();
      });
    }).start();
  }

  private void launchMenu() {
    game.setScreen(new MenuScreen(game));
  }

  @Override
  public void show() { }

  @Override
  @Deprecated
  public void render(float delta) { }

  @Override
  public void resize(int width, int height) { }

  @Override
  public void pause() { }

  @Override
  public void resume() { }

  @Override
  public void hide() { }

  @Override
  public void dispose() { }
}
