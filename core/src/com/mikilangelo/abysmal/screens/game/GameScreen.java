package com.mikilangelo.abysmal.screens.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mikilangelo.abysmal.AbysmalSpace;
import com.mikilangelo.abysmal.screens.game.components.CollisionHandler;
import com.mikilangelo.abysmal.screens.game.controllers.GameController;
import com.mikilangelo.abysmal.screens.game.controllers.ZoomController;
import com.mikilangelo.abysmal.shared.MusicPlayer;
import com.mikilangelo.abysmal.shared.Settings;
import com.mikilangelo.abysmal.shared.repositories.AsteroidsRepository;
import com.mikilangelo.abysmal.shared.repositories.ExplosionsRepository;
import com.mikilangelo.abysmal.shared.repositories.HolesRepository;
import com.mikilangelo.abysmal.shared.repositories.LasersRepository;
import com.mikilangelo.abysmal.shared.repositories.ParticlesRepository;
import com.mikilangelo.abysmal.shared.repositories.StarsRepository;
import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;
import com.mikilangelo.abysmal.screens.game.enemies.EnemiesProcessor;
import com.mikilangelo.abysmal.screens.game.actors.ship.PlayerShip;
import com.mikilangelo.abysmal.screens.game.actors.decor.animations.Portal;
import com.mikilangelo.abysmal.screens.game.actors.decor.animations.Shine;
import com.mikilangelo.abysmal.screens.game.actors.basic.StaticObject;
import com.mikilangelo.abysmal.screens.game.actors.decor.Planet;
import com.mikilangelo.abysmal.screens.game.actors.decor.Star;
import com.mikilangelo.abysmal.screens.game.actors.decor.StaticStar;
import com.mikilangelo.abysmal.screens.game.components.Camera;
import com.mikilangelo.abysmal.screens.game.uiElements.Indicator;
import com.mikilangelo.abysmal.screens.game.uiElements.Radar;
import com.mikilangelo.abysmal.screens.menu.MenuScreen;


public class GameScreen implements Screen {

  public static final float SCREEN_HEIGHT = 25;
  public static float SCREEN_WIDTH = 25;
  public static World world;
  private final PlayerShip ship;
  public static boolean screenUnderControl = false;

  public static int HEIGHT;
  public static int WIDTH;

  public static Camera camera;

  public Radar radar;
  final Array<Star> stars = new Array<>();
  final Array<StaticObject> nearObjects = new Array<>();
  final Array<StaticObject> farObjects = new Array<>();
  final AbysmalSpace game;
  Box2DDebugRenderer debugRenderer;
  Texture background;
  Sound got = Gdx.audio.newSound(Gdx.files.internal("sounds/got.mp3"));
  final Indicator healthIndicator, ammoIndicator;

  FrameBuffer frameBuffer;
  SpriteBatch shaderBatch;
  ShaderProgram shader;

  Shine shine;
  Portal portal;
  private short FPS = 0;
  private short framesPassed = 0;
  private float period = 0;

  public static EnemiesProcessor enemiesProcessor;

  public GameScreen(final AbysmalSpace game, PlayerShip ship, EnemiesProcessor processor) {
    MusicPlayer.start("sounds/battle2.mp3", 0.5f);
    this.game = game;
    this.ship = ship;
    HEIGHT = Gdx.graphics.getHeight();
    WIDTH = Gdx.graphics.getWidth();
    camera = new Camera(HEIGHT, WIDTH, ship.def.minZoom, ship.def.maxZoom);
    SCREEN_WIDTH = (float) WIDTH / (float) HEIGHT * SCREEN_HEIGHT;
    world = new World(new Vector2(0, 0), true);
    world.setContactListener(new CollisionHandler(ship.generationId));
    debugRenderer = new Box2DDebugRenderer();
    ship.createBody(world);
    ship.activateShield();
    radar = new Radar(ship.def.radarPower, ship.def.maxSpeed, HEIGHT, WIDTH);
    AsteroidsRepository.generateAsteroids(ship.x, ship.y);
    ExplosionsRepository.init();
    enemiesProcessor = processor;
    enemiesProcessor.generateEnemies(ship);

    healthIndicator = new Indicator("health", "red",
            new Vector3(179, 65, 80).scl(1 / 255f),
            game.digits, (int) ship.def.health, 6, HEIGHT);
    ammoIndicator = new Indicator("ammo", "yellow",
            new Vector3(186, 117, 67).scl(1 / 255f),
            game.digits, ship.def.ammo, 12 + 64, HEIGHT);

    game.controller.init(ship, WIDTH, HEIGHT);

    if (Settings.drawBackground) {
      generateHoles();
      generateStars();
      background = TexturesRepository.get("back.png");
      shine = new Shine(490, -1419);
    }
    generatePlanets();
    portal = new Portal(-5, 1, SCREEN_HEIGHT);
  }

  public static void shakeCamera(float power) {
    camera.shake(power);
  }

  private void generatePlanets() {
    nearObjects.add(new Planet("Terra", 4.1f, 100, 100, 0.84f));
    nearObjects.add(new Planet("moon", 2.8f, 103, 97, 0.75f));
    // planets.add(new AnimatedPlanet("nebula", 3f, -200, -50, 0.8f, 60));

    farObjects.add(new Planet("Magrateya0", 35f, -100, -100, 0.86f));
    nearObjects.add(new Planet("Magrateya1", 11.1f, -100, -100, 0.839f));
    nearObjects.add(new Planet("Magrateya2", 9.3f, -100, -100, 0.8375f));
    nearObjects.add(new Planet("Magrateya3", 9.1f, -100, -100, 0.836f));
    nearObjects.add(new Planet("Magrateya4", 9.1f, -100, -100, 0.8345f));
  }

  @Override
  public void render(float delta) {
    try {
      world.step(delta, 1, 1);
    } catch (Exception e) {
      e.printStackTrace();
    }
    handleControls(delta);
    camera.update(ship, game.objectsBatch, game.backgroundBatch, shaderBatch);
    drawBackground(delta);
    drawObjects(delta);
    // debugRenderer.render(world, camera.combined());
    drawInterface(delta);
    if (ship.bodyData.getHealth() < 0) {
      dispose();
      game.setScreen(new MenuScreen(game));
    }
    period += delta;
    framesPassed += 1;
    if (period >= 1) {
      FPS = framesPassed;
      if (FPS < 47) {
        MusicPlayer.stopMusicFade();
      }
      period = framesPassed = 0;
    }
  }

  private void handleControls(float delta) {
    ship.move(delta);
    screenUnderControl = game.controller.process(ship, camera, delta);
  }

  private void drawObjects(float delta) {
    game.objectsBatch.begin(); {
      if (Settings.drawBackground) {
        for (StaticObject p : nearObjects) {
          p.draw(game.objectsBatch, camera.X, camera.Y, camera.zoom * 1.2f);
        }
      }
      portal.draw(game.objectsBatch, delta, camera.X, camera.Y, camera.zoom);
      ParticlesRepository.drawAll(game.objectsBatch, delta);
      LasersRepository.drawSimple(game.objectsBatch, delta);
      enemiesProcessor.process(ship, delta);
      enemiesProcessor.drawAll(game.objectsBatch, delta);
      ship.draw(game.objectsBatch, delta);
      LasersRepository.drawTurrets(game.objectsBatch, delta);
      ExplosionsRepository.drawLaserExplosions(game.objectsBatch, delta);
      AsteroidsRepository.drawAll(game.objectsBatch, ship.body.getPosition(), delta);
      ExplosionsRepository.drawSimpleExplosions(game.objectsBatch, delta);
    }
    game.objectsBatch.end();
  }

  private void drawBackground(float delta) {
    if (!Settings.drawBackground) {
      return;
    }
    if (Settings.showBlackHoles) {
      frameBuffer.begin(); {
        drawBackgroundAt(shaderBatch, delta, 1.2f);
      }
      frameBuffer.end();
      TextureRegion backgroundTexture = new TextureRegion(frameBuffer.getColorBufferTexture());
      game.backgroundBatch.begin(); {
        HolesRepository.setUpShader(shader, camera.X, camera.Y, camera.zoom);
        game.backgroundBatch.draw(backgroundTexture,
                camera.X - WIDTH / 1.6667f * camera.zoom / camera.screenCoefficient,
                camera.Y + HEIGHT / 1.6667f * camera.zoom / camera.screenCoefficient,
                WIDTH * 1.2f * camera.zoom / camera.screenCoefficient,
                - HEIGHT * 1.2f * camera.zoom / camera.screenCoefficient);
      }
      game.backgroundBatch.end();
    } else {
      drawBackgroundAt(game.backgroundBatch, delta, 1.1f);
    }
  }

  private void drawBackgroundAt(Batch batch, float delta, float scale) {
    batch.begin();
    if (Settings.cameraRotation) {
      batch.draw(background,
              camera.X - WIDTH * 0.5f * camera.zoom * scale / camera.screenCoefficient,
              camera.Y - HEIGHT * 0.5f * camera.zoom * scale / camera.screenCoefficient,
              WIDTH * camera.zoom * scale / camera.screenCoefficient,
              HEIGHT * camera.zoom * scale / camera.screenCoefficient);
      batch.draw(background,
              camera.X + WIDTH * 0.5f * camera.zoom * scale / camera.screenCoefficient,
              camera.Y - HEIGHT * 0.5f * camera.zoom * scale / camera.screenCoefficient,
              WIDTH * camera.zoom * scale / camera.screenCoefficient,
              HEIGHT * camera.zoom * scale / camera.screenCoefficient);
      batch.draw(background,
              camera.X - WIDTH * 0.5f * camera.zoom * scale / camera.screenCoefficient,
              camera.Y + HEIGHT * 0.5f * camera.zoom * scale / camera.screenCoefficient,
              WIDTH * camera.zoom * scale / camera.screenCoefficient,
              HEIGHT * camera.zoom * scale / camera.screenCoefficient);
      batch.draw(background,
              camera.X - WIDTH * 1.5f * camera.zoom * scale / camera.screenCoefficient,
              camera.Y - HEIGHT * 0.5f * camera.zoom * scale / camera.screenCoefficient,
              WIDTH * camera.zoom * scale / camera.screenCoefficient,
              HEIGHT * camera.zoom * scale / camera.screenCoefficient);
      batch.draw(background,
              camera.X - WIDTH * 0.5f * camera.zoom * scale / camera.screenCoefficient,
              camera.Y - HEIGHT * 1.5f * camera.zoom * scale / camera.screenCoefficient,
              WIDTH * camera.zoom * scale / camera.screenCoefficient,
              HEIGHT * camera.zoom * scale / camera.screenCoefficient);
    } else {
      batch.draw(background,
              camera.X - WIDTH / 2f * camera.zoom * scale / camera.screenCoefficient,
              camera.Y - HEIGHT / 2f * camera.zoom * scale / camera.screenCoefficient,
              WIDTH * camera.zoom * scale / camera.screenCoefficient,
              HEIGHT * camera.zoom * scale / camera.screenCoefficient);
    }
    shine.draw(batch, delta, camera.X, camera.Y, camera.zoom);
    for (StaticObject o: farObjects) {
      o.draw(batch, camera.X, camera.Y, camera.zoom * scale);
    }
    for (Star s : stars) {
      s.draw(batch, camera.X, camera.Y, camera.zoom * scale);
    }
    StarsRepository.draw(batch, camera.X, camera.Y, camera.zoom * scale);
    batch.end();
  }

	private void drawInterface(float delta) {
		game.batchInterface.begin(); {
		  game.controller.drawInterface(game.batchInterface);
      radar.drawBack(game.batchInterface);
			AsteroidsRepository.drawAtRadar(game.batchInterface, radar);
      enemiesProcessor.drawAtRadar(game.batchInterface, radar);
      radar.draw(game.batchInterface, game.digits, game.customFont, ship.angle);
			// game.customFont.getData().setScale(HEIGHT / 1400f);
			healthIndicator.draw(game.batchInterface, game.digits, ship.bodyData.getHealth());
      ammoIndicator.draw(game.batchInterface, game.digits, ship.ammo);
			game.simpleFont.draw(game.batchInterface, "FPS: " + FPS, 10, HEIGHT - 20);
//			game.customFont.draw(game.batchInterface, "x: " + Math.round(cameraX) + ", y: " + Math.round(cameraY), 10, HEIGHT - 80);
		}
		game.batchInterface.end();
	}

	private void generateHoles() {
    if (!Settings.showBlackHoles) {
      return;
    }
    try {
      frameBuffer = new FrameBuffer(Pixmap.Format.RGB565, WIDTH, HEIGHT, false);
    } catch (GdxRuntimeException e) {
      frameBuffer = new FrameBuffer(Pixmap.Format.RGB565, WIDTH, HEIGHT, false);
    }
    shaderBatch = new SpriteBatch();
    this.shader = new ShaderProgram(
            Gdx.files.internal("shaders/whole/vertex.glsl"),
            Gdx.files.internal("shaders/whole/fragment.glsl"));
    ShaderProgram.pedantic = false;
    game.backgroundBatch.setShader(shader);
    HolesRepository.generateHoles(ship.x, ship.y);
  }

	private void generateStars() {
    Star.initTexture();
    // StarsRepository.generateGalaxy(-100, 50);
    for (int i = 0; i < 370; i++) {
      stars.add(new StaticStar(MathUtils.random(0.93f, 0.98f), 0.36f));
    }
    for (int i = 0; i < 120; i++) {
      stars.add(new StaticStar(MathUtils.random(0.87f, 0.92f), 0.51f));
    }
    for (int i = 0; i < 45; i++) {
      stars.add(new StaticStar(MathUtils.random(0.81f, 0.85f), 0.67f));
    }
  }

  @Override
  public void resize(int width, int height) {
    if (width * height == 0) return;
    WIDTH = width; HEIGHT = height;
    SCREEN_WIDTH = (float) WIDTH / (float) HEIGHT * SCREEN_HEIGHT;
    camera.resize(height, width);
    game.cameraInterface.setToOrtho(false, width, height);
    game.batchInterface.setProjectionMatrix(game.cameraInterface.combined);
    radar.resize(height, width);
    Indicator.handleResize(height);
    healthIndicator.resize(height);
    ammoIndicator.resize(height);
    game.controller.resizeComponents(width, height);
  }

  @Override
  public void dispose() {
    enemiesProcessor.dispose();
    ParticlesRepository.clear();
    HolesRepository.clear();
    ExplosionsRepository.clear();
    LasersRepository.clear();
    AsteroidsRepository.clear();
    ship.destroy(world);
    world.dispose();
    stars.clear();
    if (shader != null) {
      shader.dispose();
    }
    game.controller.dispose();
    got.dispose();
  }

  @Override
  public void show() {
    System.out.println("show method called");
    Gdx.input.setInputProcessor(new GestureDetector(new ZoomController()));
  }

  @Override
  public void pause() {
    //super.pause();
  }

  @Override
  public void resume() {}

  @Override
  public void hide() {}
}
