package com.mikilangelo.abysmal.ui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.mikilangelo.abysmal.AbysmalSpace;
import com.mikilangelo.abysmal.components.ShipDefinitions;
import com.mikilangelo.abysmal.components.repositories.SoundsRepository;
import com.mikilangelo.abysmal.components.repositories.TexturesRepository;
import com.mikilangelo.abysmal.models.game.extended.Asteroid;

public class LoadingScreen implements Screen {

  final AbysmalSpace game;
  OrthographicCamera camera;

  public LoadingScreen(AbysmalSpace abysmalSpace) {
    this.game = abysmalSpace;
    camera = new OrthographicCamera();
    camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
    new Thread(new Runnable() {
      @Override
      public void run() {
        SoundsRepository.init();
        Gdx.app.postRunnable(new Runnable() {
          @Override
          public void run() {
            TexturesRepository.init();
            ShipDefinitions.init();
            Asteroid.init();
            launchMenu();
          }
        });
      }
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
