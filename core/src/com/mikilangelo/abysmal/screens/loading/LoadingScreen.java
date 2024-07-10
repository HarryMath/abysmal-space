package com.mikilangelo.abysmal.screens.loading;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.mikilangelo.abysmal.EnigmaSpace;
import com.mikilangelo.abysmal.shared.Settings;
import com.mikilangelo.abysmal.shared.ShipDefinitions;
import com.mikilangelo.abysmal.shared.repositories.PlanetsRepository;
import com.mikilangelo.abysmal.shared.repositories.SoundsRepository;
import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;
import com.mikilangelo.abysmal.screens.game.actors.fixtures.Asteroid;
import com.mikilangelo.abysmal.screens.game.uiElements.Indicator;
import com.mikilangelo.abysmal.screens.menu.MenuScreen;

public class LoadingScreen implements Screen {

  final EnigmaSpace game;
  OrthographicCamera camera;
  Preferences storage;

  public LoadingScreen(EnigmaSpace abysmalSpace) {
    this.game = abysmalSpace;
    camera = new OrthographicCamera();
    camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
    new Thread(() -> {
      SoundsRepository.init();
      Gdx.app.postRunnable(() -> {
        TexturesRepository.init();
        ShipDefinitions.init();
        PlanetsRepository.init();
        Asteroid.init();
        Indicator.init();
      });
      Gdx.app.postRunnable(() -> {
        storage = Gdx.app.getPreferences("storage");
        Settings.isFullscreen = storage.contains("isFullscreen") && storage.getBoolean("isFullscreen");
        Settings.cameraRotation = storage.contains("cameraRotation") && storage.getBoolean("cameraRotation");
        Settings.drawBackground = !storage.contains("drawBackground") || storage.getBoolean("drawBackground");
        Settings.fixedPosition = !storage.contains("drawBackground") || storage.getBoolean("drawBackground");
        Settings.drawBlackHoles = storage.contains("drawBlackHoles") && storage.getBoolean("drawBlackHoles");
        Settings.withParticles = !storage.contains("withParticles") || storage.getBoolean("withParticles");
        final int currentShipIndex = storage.contains("shipId") ? storage.getInteger("shipId") : 0;
        launchMenu(currentShipIndex);
      });
    }).start();
  }

  private void launchMenu(int shipIndex) {
    game.setScreen(new MenuScreen(game, shipIndex));
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
