package com.mikilangelo.abysmal.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mikilangelo.abysmal.AbysmalSpace;
import com.mikilangelo.abysmal.screens.menu.components.NotificationWrapper;
import com.mikilangelo.abysmal.screens.menu.components.ServerProvider;
import com.mikilangelo.abysmal.screens.menu.options.LocalClientOption;
import com.mikilangelo.abysmal.screens.menu.options.LocalGameOption;
import com.mikilangelo.abysmal.screens.menu.options.LocalServerOption;
import com.mikilangelo.abysmal.shared.MusicPlayer;
import com.mikilangelo.abysmal.shared.ShipDefinitions;
import com.mikilangelo.abysmal.shared.repositories.SoundsRepository;
import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;
import com.mikilangelo.abysmal.screens.game.enemies.EnemiesProcessor;
import com.mikilangelo.abysmal.screens.game.enemies.bots.BotsProcessor;
import com.mikilangelo.abysmal.screens.game.enemies.online.UdpClient;
import com.mikilangelo.abysmal.shared.defenitions.ShipDef;
import com.mikilangelo.abysmal.screens.game.actors.ship.PlayerShip;
import com.mikilangelo.abysmal.screens.game.actors.decor.animations.EngineAnimation;
import com.mikilangelo.abysmal.screens.game.GameScreen;
import com.mikilangelo.abysmal.shared.tools.Async;
import com.mikilangelo.abysmal.shared.tools.CalculateUtils;
import com.mikilangelo.abysmal.screens.menu.options.ExitOption;
import com.mikilangelo.abysmal.screens.menu.options.MainMenuOption;
import com.mikilangelo.abysmal.screens.menu.options.GlobalGameOption;
import com.mikilangelo.abysmal.screens.menu.options.Option;
import com.mikilangelo.abysmal.screens.menu.options.PlayOption;
import com.mikilangelo.abysmal.screens.menu.options.SettingsOption;
import com.mikilangelo.abysmal.screens.menu.options.SingleplayerOption;

import java.io.IOException;

public class MenuScreen implements Screen {

  final AbysmalSpace game;
  OrthographicCamera camera;
  int w, h;
  Preferences storage;
  Texture background = TexturesRepository.get("backMenu.png");
  Texture logo = TexturesRepository.get("UI/logo.png");
  GlyphLayout version;
  GlyphLayout copyright;
  private final float logoRatio;
  private final Array<MenuStar> stars = new Array<>();
  Sprite star = new Sprite(TexturesRepository.get("starMenu.png"));
  Sprite arrow = new Sprite(TexturesRepository.get("UI/arrow.png"));
  private final Array<EngineAnimation> engineAnimations = new Array<>();
  private ShipDef currentShip = null;
  private ShipDef previousShip = null;
  private int currentShipIndex = 0;
  private int prevShipIndex = 0;
  private boolean shipSelected = false;
  private float currentShipY = 0;
  private float prevShipY = 1f;

  private final ShapeRenderer shapeRenderer = new ShapeRenderer();
  private final Array<Option> options = new Array<>();
  private float optionHeight;
  private float menuWidth;
  private float menuWidthSqrt;
  private float menuStartY;
  private float menuStartX = 0;
  private float menuAnimationCounter = 0;
  private float t = 0;
  private float darkness = 1;
  private float leftArrowX, rightArrowX, arrowY;

  final NotificationWrapper notification = new NotificationWrapper();
  boolean isLoading = false;
  float overlayOpacity = 0;
  String loadingText = "";
  EnemiesProcessor enemiesProcessor;
  long seed = 32323;
  final Sound positive = SoundsRepository.getSound("sounds/button_positive.mp3");
  final Sound negative = SoundsRepository.getSound("sounds/button_negative.mp3");

  public MenuScreen(AbysmalSpace game) {
    this.w = Gdx.graphics.getWidth();
    this.h = Gdx.graphics.getHeight();
    MusicPlayer.start("sounds/menu.mp3", 0.6f);
    this.game = game;
    camera = new OrthographicCamera();
    camera.setToOrtho(false, w, h);
    logoRatio = logo.getWidth() / (float) logo.getHeight();
    storage = Gdx.app.getPreferences("storage");
    currentShipIndex = storage.contains("shipId") ? storage.getInteger("shipId") : 0;
    selectShip(currentShipIndex);
    for (ShipDef d: ShipDefinitions.shipDefinitions) {
      this.engineAnimations.add(new EngineAnimation(d.engineAnimation, d.frameFrequency));
    }
    game.simpleFont.setColor(1, 1, 1, 0.8f);
    handleMainMenuOption();
    generateStars();
    resize(w, h);
  }

  private void generateStars() {
    for (int i = 0; i < 70; i++) {
      stars.add(new MenuStar(
              MathUtils.random(0f, 1f), MathUtils.random(0f, 1f),
              MathUtils.random(1.2f, 1.7f), MathUtils.random(0.25f, 0.45f)));
    }
    for (int i = 0; i < 20; i++) {
      stars.add(new MenuStar(
              MathUtils.random(0f, 1f), MathUtils.random(0f, 1f),
              MathUtils.random(1.6f, 2.4f), MathUtils.random(0.55f, 0.8f)));
    }
    for (int i = 0; i < 10; i++) {
      stars.add(new MenuStar(
              MathUtils.random(0f, 1f), MathUtils.random(0f, 1f),
              MathUtils.random(2.3f, 2.7f), MathUtils.random(1f, 1.4f)));
    }
  }

  public void handlePlayClick() {
    this.options.clear();
    this.options.add(new SingleplayerOption(), new GlobalGameOption(), new LocalGameOption(), new MainMenuOption());
  }

  public void handleMainMenuOption() {
    this.options.clear();
    this.options.add(new PlayOption(), new SettingsOption(), new ExitOption());
  }

  public void setSingleplayer() {
    this.enemiesProcessor = new BotsProcessor();
    this.startGame();
  }

  public void setLocalMultiPlayer() {
    this.options.clear();
    this.options.add(new LocalClientOption(), new LocalServerOption(), new PlayOption("< Back"));
  }

  public void setGlobalMultiplayer() {
    this.isLoading = true;
    this.loadingText = "searching for server";
    new Async(ServerProvider.findGlobalServer).then(() -> {
      if (ServerProvider.server != null) {
        try {
          this.enemiesProcessor = new UdpClient(ServerProvider.server.ip, ServerProvider.server.udpPort);
          this.seed = ServerProvider.server.seed;
          this.startGame();
        } catch (IOException e) {
          this.notification.showWarning("Error connecting to server");
          negative.play(1);
          e.printStackTrace();
        }
      } else {
        negative.play(1);
        this.notification.showWarning("No available server at the moment. Try later Please. We are very sorry for tat inconvenience! turn back later, please");
      }
      isLoading = false;
    }).start();
  }

  private void startGame() {
    Gdx.app.postRunnable(()-> {
      game.setScreen(new GameScreen(
              game,
              new PlayerShip(currentShip, 0 , 0),
              this.enemiesProcessor, seed));
      dispose();
    });
  }

  public void handleSettingsOption() {
    notification.showWarning("Settings unavailable yet :c");
  }

  private void animateShips(float delta) {
    if (shipSelected) return;
    prevShipY += prevShipY >= 0 ? prevShipY / 25f + delta * 0.1f : delta * 0.2f;
    if (prevShipY > 0.5f) {
      currentShipY = currentShipY * 40f / 41f;
    }
    if (currentShipY > - 0.001f) {
      currentShipY = 0;
      shipSelected = true;
    }
  }

  private void selectShip(int i) {
    if (prevShipY < 0.5f || i > ShipDefinitions.shipDefinitions.size - 1 || i < 0) return;
    prevShipIndex = currentShipIndex;
    currentShipIndex = i;
    previousShip = currentShip;
    currentShip = ShipDefinitions.get(currentShipIndex);
    currentShip.bodyTexture.setRotation(90);
    resizeShip(currentShip);
    prevShipY = currentShipY;
    currentShipY = -1f;
    shipSelected = false;
    storage.putInteger("shipId", currentShipIndex);
    storage.flush();
  }

  private void resizeShip(ShipDef ship) {
    ship.resizeTextures(h * 0.06f);
  }

  @Override
  public void show() {
    Gdx.input.setInputProcessor(new ActionDetector(this));
  }

  @Override
  public void render(float delta) {
    Gdx.gl.glClearColor(0.05f, 0.06f, 0.08f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    updateMenu(delta);
    camera.update();
    game.batchInterface.setProjectionMatrix(camera.combined);
    drawBackground(delta);
    drawMenu();
    drawInterface();
    drawLoadingOverlay();
    drawSmoothDarkness();
  }

  private void updateMenu(float delta) {
    menuStartY = h / 2f + options.size / 2f * optionHeight;
    if (menuAnimationCounter < menuWidthSqrt * 1.2f) {
      menuAnimationCounter += delta * 17 + menuAnimationCounter / 10f;
      menuStartX = - (menuAnimationCounter - menuWidthSqrt) * (menuAnimationCounter - menuWidthSqrt);
      game.customFont.setColor(1, 1, 1, 0);
    }
    else if (menuAnimationCounter < menuWidthSqrt * 2) {
      game.customFont.setColor(1, 1, 1,
              Math.min( (menuAnimationCounter - menuWidthSqrt * 1.2f) / menuWidthSqrt, 1));
      menuAnimationCounter += delta * 80;
      menuStartX = - (menuWidth * 0.04f);
    } else {
      game.customFont.setColor(1,1,1,1);
    }
  }

  private void drawBackground(float delta) {
    game.batchInterface.begin();
    game.batchInterface.draw(background, 0, 0, w, h);
    for (MenuStar s : stars) {
      star.setScale(s.scale, s.scale * s.length);
      star.setAlpha(s.opacity);
      s.y -= delta * 0.8f * s.layer * s.layer;
      if (s.y < 0) s.y = 1 + s.y;
      star.setCenter(s.x * w, s.y * h);
      star.draw(game.batchInterface);
    }
    t += delta;
    animateShips(delta);
    drawShip(currentShip, engineAnimations.get(currentShipIndex),
            w * 0.75f + h * 0.01f * MathUtils.cos(t * 1.2f),
            h * 0.45f + h * currentShipY + h * 0.015f * MathUtils.sin(t * 0.9f), delta);
    if (previousShip != null) {
      drawShip(previousShip, engineAnimations.get(prevShipIndex),
              w * 0.75f + h * 0.01f * MathUtils.cos(t * 1.2f),
              h * 0.45f + h * prevShipY + h * 0.015f * MathUtils.sin(t * 0.9f), delta);
    }

    game.batchInterface.draw(logo, optionHeight, h * 0.97f - optionHeight * 1.4f,
            optionHeight * 1.3f * logoRatio, optionHeight * 1.3f);
    game.batchInterface.end();
  }

  private void drawMenu() {
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    shapeRenderer.setProjectionMatrix(camera.combined);
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    final float startX = Math.min(menuStartX, 0);
    for (int i = 0; i < options.size; i++) {
      if (options.get(i).isHovered()) {
        shapeRenderer.setColor(0f, 0f, 0f, 0.5f);
        shapeRenderer.rect(startX, menuStartY - optionHeight * (i + 0.05f), menuWidth + menuStartX - startX, optionHeight * 0.9f);
      } else {
        shapeRenderer.setColor(0.03f, 0.03f, 0.031f, 0.4f);
        shapeRenderer.rect(startX, menuStartY - optionHeight * i, menuWidth + menuStartX - startX, optionHeight * 0.8f);
      }
    }
    shapeRenderer.end();
    Gdx.gl.glDisable(GL20.GL_BLEND);
  }

  private void drawInterface() {
    game.batchInterface.begin();
    {
      float opacity = currentShipIndex < ShipDefinitions.shipDefinitions.size - 1 ? 1 : 0.4f;
      arrow.setCenter(rightArrowX, arrowY);
      arrow.draw(game.batchInterface, opacity);
      arrow.flip(true, false);
      opacity = currentShipIndex > 0 ? 1 : 0.4f;
      arrow.setCenter(leftArrowX, arrowY);
      arrow.draw(game.batchInterface, opacity);
      arrow.flip(true, false);
    }
    if (currentShipY > -0.6666f) {
      game.customFont.setColor(1,1, 1, 1 + currentShipY * 1.5f);
      game.customFont.getData().setScale(optionHeight * 0.2f / game.customFont.getCapHeight() * game.customFont.getScaleY());
      GlyphLayout shipName = new GlyphLayout(game.customFont, currentShip.name);
      game.customFont.draw(game.batchInterface, shipName, w * 0.75f - shipName.width / 2, Math.min(h * 0.77f, h * 0.5f + w * 0.15f) );
    }
    game.batchInterface.draw(arrow, w * 0.9f, h * 0.45f,
            arrow.getWidth() * optionHeight / h,
            arrow.getHeight() * optionHeight / h);
    game.customFont.getData().setScale(optionHeight * 0.39f / game.customFont.getCapHeight() * game.customFont.getScaleY());
    for (int i = 0; i < options.size; i++) {
      game.customFont.setColor(1,1, 1, options.get(i).isHovered() ? 1 : 0.8f);
      game.customFont.draw(game.batchInterface, options.get(i).getText(),
              menuStartX / 3f + optionHeight * 1.05f,
              menuStartY - optionHeight * (i - 0.6f));
    }
    game.simpleFont.draw(game.batchInterface, copyright, w/2f - copyright.width / 2, copyright.height * 1.7f);
    game.simpleFont.draw(game.batchInterface, version, w - version.width - copyright.height * 0.7f, copyright.height * 1.7f);
    game.batchInterface.end();
  }

  private void drawLoadingOverlay() {
    if (isLoading || notification.isShown()) {
      Gdx.gl.glEnable(GL20.GL_BLEND);
      Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
      shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
      shapeRenderer.setColor(0.011f, 0.011f, 0.015f, 0.6f * overlayOpacity);
      shapeRenderer.rect(0, 0, w, h);
      shapeRenderer.end();
      Gdx.gl.glDisable(GL20.GL_BLEND);
      overlayOpacity = (overlayOpacity * 0.6f + 0.4f);
    } else {
      overlayOpacity *= 0.8f;
    }
    game.batchInterface.begin();
    notification.draw(game.batchInterface, game.customFont);
    game.batchInterface.end();
  }

  private void drawSmoothDarkness() {
    if (darkness > 0) {
      darkness -= 0.00047 + (1 - darkness) * 0.035f;
      if (darkness < 0) darkness = 0;
      Gdx.gl.glEnable(GL20.GL_BLEND);
      Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
      shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
      shapeRenderer.setColor(0, 0, 0, darkness);
      shapeRenderer.rect(0, 0, w, h);
      shapeRenderer.end();
      Gdx.gl.glDisable(GL20.GL_BLEND);
    }
  }

  private void drawShip(ShipDef def, EngineAnimation engine, float x, float y, float delta) {
    if (def.decorUnder != null) {
      def.decorUnder.setAlpha(0.5f);
      def.decorUnder.setCenter(x, y);
      def.decorUnder.setRotation(90);
      def.decorUnder.draw(game.batchInterface);
    }
    engine.draw(game.batchInterface, delta, x, y,MathUtils.PI * 0.5f);
    def.bodyTexture.setCenter(x, y);
    def.bodyTexture.draw(game.batchInterface);
    if (def.decorOver != null) {
      def.decorOver.setAlpha(0.5f);
      def.decorOver.setCenter(x, y);
      def.decorOver.setRotation(90);
      def.decorOver.draw(game.batchInterface);
    }
  }

  @Override
  public void resize(int width, int height) {
    if (width * height == 0) return;
    this.w = width;
    this.h = height;
    final float screenDensity = Gdx.graphics.getDensity();
    optionHeight = Math.min((60 + h * 0.18f) / 3 * (float) Math.sqrt(screenDensity), h / 8f);
    menuWidth = (Math.min(430 * (float) Math.sqrt(screenDensity), w * 0.5f) + w / 2.5f * 2) / 3f;
    menuWidthSqrt = (float) Math.sqrt(menuWidth);
    resizeShip(currentShip);
    if (previousShip != null) {
      resizeShip(previousShip);
    }
    rightArrowX = w * 0.91f;
    leftArrowX = w * 0.59f;
    arrowY = h * 0.45f;
    arrow.setScale(optionHeight * 0.9f / arrow.getHeight());
    for (MenuStar s: stars) {
      s.scale = s.layer * h * 0.02f / star.getHeight();
    }
    camera.setToOrtho(false, w, h);
    game.simpleFont.getData().setScale(
            optionHeight * 0.2f / game.simpleFont.getCapHeight() * game.simpleFont.getScaleY());
    version = new GlyphLayout(game.simpleFont, "v1.0.0. (2021-09-29)");
    copyright = new GlyphLayout(game.simpleFont, "(c) 2021 MikiIangeIo");
    this.notification.resize(width, height);
  }

  @Override
  public void pause() { }

  @Override
  public void resume() { }

  @Override
  public void hide() { }

  @Override
  public void dispose() {
    stars.clear();
    shapeRenderer.dispose();
  }

  public void setLocalClient() {
    notification.showWarning("This option is not available yet :c");
  }

  public void setLocalServer() {
    notification.showWarning("This option is not available yet :c");
  }

  private class MenuStar {
    float x, y;
    final float length, layer;
    float scale; float opacity;

    public MenuStar(float x, float y, float length, float layer) {
      this.x = x;
      this.y = y;
      opacity = Math.min(0.65f, 0.37f + layer / 3.4f) * (float) Math.sqrt(x);
      this.length = length;
      this.layer = layer;
      this.scale = layer * h * 0.02f / star.getHeight();
    }
  }

  private class ActionDetector extends GestureDetector {

    public ActionDetector(MenuScreen screen) {
      super(new Controller(screen));
    }

    @Override
    public boolean mouseMoved(int x, int y) {
      super.mouseMoved(x, y);
      y = h - y;
      boolean hovered = false;
      for (int i = 0; i < options.size; i++) {
        boolean isHovered = x < menuWidth &&
                y > menuStartY - optionHeight * i &&
                y < menuStartY - optionHeight * (i - 0.8f);
        options.get(i).setHovered(isHovered);
        hovered = hovered || isHovered;
      }
      if (notification.isShown()) {
        hovered = notification.isInButton((int) x, (int) y);
      } else {
        hovered = hovered ||
                CalculateUtils.distance(x, y, leftArrowX, arrowY) <= optionHeight * 0.5f ||
                CalculateUtils.distance(x, y, rightArrowX, arrowY) <= optionHeight * 0.5f;
      }
      Gdx.graphics.setSystemCursor(hovered ?
              Cursor.SystemCursor.Hand :
              Cursor.SystemCursor.Arrow);
      return false;
    }
  }

  private class Controller implements GestureDetector.GestureListener {

    private final MenuScreen screen;

    Controller(MenuScreen screen) {
      this.screen = screen;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
      if (isLoading) {
        return false;
      }
      y = h - y;
      if (notification.isShown()) {
        if (notification.isInButton((int) x, (int) y)) {
          positive.play(0.3f);
          notification.hide();
        }
      }
      else {
        for (int i = 0; i < options.size; i++) {
          if (x < menuWidth &&
                  y > menuStartY - optionHeight * i &&
                  y < menuStartY - optionHeight * (i - 0.8f)
          ) {
            menuAnimationCounter = 0;
            options.get(i).handleClick(screen);
            positive.play(0.3f);
            return false;
          }
        }
        if (CalculateUtils.distance(x, y, leftArrowX, arrowY) <= optionHeight * 0.5f) {
          selectShip(currentShipIndex - 1);
        } else if (CalculateUtils.distance(x, y, rightArrowX, arrowY) <= optionHeight * 0.5f) {
          selectShip(currentShipIndex + 1);
        }
      }
      return false;
    }
    public boolean touchDown(float x, float y, int pointer, int button) { return false; }
    public boolean longPress(float x, float y) { return false; }
    public boolean fling(float velocityX, float velocityY, int button) { return false; }
    public boolean pan(float x, float y, float deltaX, float deltaY) { return false; }
    public boolean panStop(float x, float y, int pointer, int button) { return false; }
    public boolean zoom(float initialDistance, float distance) { return false; }
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) { return false; }
    public void pinchStop() {}
  }
}
