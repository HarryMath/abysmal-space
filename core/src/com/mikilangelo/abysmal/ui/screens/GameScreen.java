package com.mikilangelo.abysmal.ui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
import com.mikilangelo.abysmal.components.CollisionHandler;
import com.mikilangelo.abysmal.components.GameController;
import com.mikilangelo.abysmal.components.MusicPlayer;
import com.mikilangelo.abysmal.components.Settings;
import com.mikilangelo.abysmal.components.TouchHandler;
import com.mikilangelo.abysmal.components.repositories.AsteroidsRepository;
import com.mikilangelo.abysmal.components.repositories.ExplosionsRepository;
import com.mikilangelo.abysmal.components.repositories.HolesRepository;
import com.mikilangelo.abysmal.components.repositories.LasersRepository;
import com.mikilangelo.abysmal.components.repositories.ParticlesRepository;
import com.mikilangelo.abysmal.components.repositories.TexturesRepository;
import com.mikilangelo.abysmal.enemies.EnemiesProcessor;
import com.mikilangelo.abysmal.models.game.PlayerShip;
import com.mikilangelo.abysmal.models.game.animations.Portal;
import com.mikilangelo.abysmal.models.game.animations.Shine;
import com.mikilangelo.abysmal.models.game.basic.StaticObject;
import com.mikilangelo.abysmal.models.game.extended.Planet;
import com.mikilangelo.abysmal.models.game.extended.Star;
import com.mikilangelo.abysmal.models.objectsData.DestroyableObjectData;
import com.mikilangelo.abysmal.ui.gameElemets.Indicator;
import com.mikilangelo.abysmal.ui.gameElemets.Joystick;
import com.mikilangelo.abysmal.ui.gameElemets.Radar;
import com.mikilangelo.abysmal.ui.gameElemets.Shooter;


public class GameScreen implements Screen {

  public static final float SCREEN_HEIGHT = 25;
  public static float SCREEN_WIDTH = 25;
  public static World world;
  private final PlayerShip ship;
  public static boolean screenUnderControl = false;
  private DestroyableObjectData shipData;
  public static float cameraX = 0, cameraY = 0;

  public static int HEIGHT;
  public static int WIDTH;

  public static float cameraScreenCoefficient;
  public static float initialZoomCoefficient;
  public static float speedZoomCoefficient;
  private float cameraBiasX, cameraBiasY;
  static OrthographicCamera camera;


  Joystick joystick;
  Shooter shooter;
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

  final TouchHandler touch1Handler;
  final TouchHandler touch2Handler;

  Shine shine;
  Portal portal;
  private short FPS = 0;
  private short framesPassed = 0;
  private float period = 0;

  public static EnemiesProcessor enemiesProcessor;

  public GameScreen(final AbysmalSpace game, PlayerShip ship, EnemiesProcessor processor) {
    MusicPlayer.start("sounds/battle2.mp3", 0.5f);
    this.game = game;
    camera = new OrthographicCamera();
    HEIGHT = Gdx.graphics.getHeight();
    WIDTH = Gdx.graphics.getWidth();
    radar = new Radar(ship.definition.radarPower);
    SCREEN_WIDTH = (float) WIDTH / (float) HEIGHT * SCREEN_HEIGHT;
    cameraScreenCoefficient = HEIGHT / SCREEN_HEIGHT;
    camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);
    initialZoomCoefficient = 1f;
    speedZoomCoefficient = 1.5f;
    camera.zoom = initialZoomCoefficient * speedZoomCoefficient;
    world = new World(new Vector2(0, 0), true);
    world.setContactListener(new CollisionHandler());
    debugRenderer = new Box2DDebugRenderer();
    ship.createBody(world);
    ship.activateShield();
    shipData = ((DestroyableObjectData) ship.body.getUserData());
    this.ship = ship;
    joystick = new Joystick(HEIGHT / 8);
    shooter = new Shooter(ship.turrets.size > 0);
    AsteroidsRepository.generateAsteroids(ship.x, ship.y);
    ExplosionsRepository.init();
    enemiesProcessor = processor;
    enemiesProcessor.generateEnemies(ship);

    healthIndicator = new Indicator("health", "red",
            new Vector3(0.925f, 0.365f, 0.435f),
            game.digits, (int) ship.definition.health, 6, HEIGHT);
    ammoIndicator = new Indicator("ammo", "yellow",
            new Vector3(0.9725f, 0.769f, 0.4f),
            game.digits, (int) ship.definition.health, 12 + 64, HEIGHT);

    if (Settings.drawBackground) {
      generateHoles();
      generateStars();
      background = TexturesRepository.get("back.png");
      shine = new Shine(490, -1419);
    }
    generatePlanets();
    portal = new Portal(-5, 1, SCREEN_HEIGHT);
    touch1Handler = new TouchHandler(ship);
    touch2Handler = new TouchHandler(ship);
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
    updateCamera();
    drawBackground(delta);
    drawObjects(delta);
    // debugRenderer.render(world, camera.combined);
    drawInterface(delta);
    if (shipData.getHealth() < 0) {
      dispose();
      game.setScreen(new MenuScreen(game));
    }
    period += delta;
    framesPassed += 1;
    if (period >= 1) {
      FPS = framesPassed;
      period = framesPassed = 0;
    }
  }

  private void handleControls(float delta) {
    ship.move(delta);
    screenUnderControl = false;
    if (!game.isSensor) {
      ship.newAngle = ship.angle;
      if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
        ship.applyImpulse(1, true);
      }
      if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
        ship.applyImpulse(-0.01f, false);
      }
      if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
        ship.rotate(0.7f, delta);
      }
      if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
        ship.rotate(-0.7f, delta);
      }
      if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
        if (initialZoomCoefficient < ship.definition.maxZoom) {
          initialZoomCoefficient += 0.01;
        }
      }
      if (Gdx.input.isKeyPressed(Input.Keys.Z)) {
        if (initialZoomCoefficient > 0.4) {
          initialZoomCoefficient -= 0.01;
        }
      }
    }

      if (Gdx.input.isTouched(1)) {
        touch2Handler.touchX = Gdx.input.getX(1);
        touch2Handler.touchY = Gdx.input.getY(1);
        screenUnderControl = touch2Handler.handleTouch(joystick, shooter, delta);
      } else {
        touch2Handler.endTouch();
      }
      if (Gdx.input.isTouched(0)) {
        touch1Handler.touchX = Gdx.input.getX(0);
        touch1Handler.touchY = Gdx.input.getY(0);
        screenUnderControl = touch1Handler.handleTouch(joystick, shooter, delta);
      } else {
        touch1Handler.endTouch();
      }
  }

  private void updateCamera() {
    speedZoomCoefficient = (ship.body.getLinearVelocity().len() * 0.06f + speedZoomCoefficient * 39f) / 40f;
    if (Settings.fixedPosition) {
      cameraBiasX = cameraBiasY = 0;
    } else {
      cameraBiasX = (cameraBiasX * 87 + MathUtils.cos(ship.newAngle) * speedZoomCoefficient * 7) / 88f;
      cameraBiasY = (cameraBiasY * 87 + MathUtils.sin(ship.newAngle) * speedZoomCoefficient * 7) / 88f;
    }
    cameraX = ship.body.getPosition().x + cameraBiasX;
    cameraY = ship.body.getPosition().y + cameraBiasY;
    camera.position.set(cameraX, cameraY, 0);
    camera.zoom = (speedZoomCoefficient + 1) * (initialZoomCoefficient + 1) * 0.5f - 0.5f;
    camera.update();
    game.objectsBatch.setProjectionMatrix(camera.combined);
    if (Settings.drawBackground) {
      game.backgroundBatch.setProjectionMatrix(camera.combined);
      if (Settings.showBlackHoles) {
        shaderBatch.setProjectionMatrix(camera.combined);
      }
    }
  }

  private void drawObjects(float delta) {
    game.objectsBatch.begin(); {
      if (Settings.drawBackground) {
        for (StaticObject p : nearObjects) {
          p.draw(game.objectsBatch, cameraX, cameraY, camera.zoom * 1.2f);
        }
      }
      portal.draw(game.objectsBatch, delta, cameraX, cameraY, camera.zoom);
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
        HolesRepository.setUpShader(shader, cameraX, cameraY, camera.zoom);
        game.backgroundBatch.draw(backgroundTexture,
                cameraX - WIDTH / 1.6667f * camera.zoom / cameraScreenCoefficient,
                cameraY + HEIGHT / 1.6667f * camera.zoom / cameraScreenCoefficient,
                WIDTH * 1.2f * camera.zoom / cameraScreenCoefficient,
                - HEIGHT * 1.2f * camera.zoom / cameraScreenCoefficient);
      }
      game.backgroundBatch.end();
    } else {
      drawBackgroundAt(game.backgroundBatch, delta, 1.1f);
    }
  }

  private void drawBackgroundAt(Batch batch, float delta, float scale) {
    batch.begin();
    batch.draw(background,
            cameraX - WIDTH / 2f * camera.zoom * scale / cameraScreenCoefficient,
            cameraY - HEIGHT / 2f * camera.zoom * scale / cameraScreenCoefficient,
            WIDTH * camera.zoom * scale / cameraScreenCoefficient,
            HEIGHT * camera.zoom * scale / cameraScreenCoefficient);
    shine.draw(batch, delta, cameraX, cameraY, camera.zoom);
    for (StaticObject o: farObjects) {
      o.draw(batch, cameraX, cameraY, camera.zoom * scale);
    }
    for (Star s : stars) {
      s.draw(batch, cameraX, cameraY, camera.zoom * scale);
    }
    batch.end();
  }

	private void drawInterface(float delta) {
		game.batchInterface.begin(); {
		  shooter.draw(game.batchInterface);
		  joystick.draw(game.batchInterface);
      radar.draw(game.batchInterface);
			AsteroidsRepository.drawAtRadar(game.batchInterface, radar, cameraX, cameraY);
      enemiesProcessor.drawAtRadar(game.batchInterface, radar, cameraX, cameraY);
			// game.customFont.getData().setScale(HEIGHT / 1400f);
			healthIndicator.draw(game.batchInterface, game.digits, shipData.getHealth());
			//game.simpleFont.draw(game.batchInterface, "FPS: " + FPS, 10, HEIGHT - 20);
			//game.simpleFont.draw(game.batchInterface, "speed: " + Math.round(ship.body.getLinearVelocity().len()), 10, HEIGHT - 50);
//			game.customFont.draw(game.batchInterface, "x: " + Math.round(cameraX) + ", y: " + Math.round(cameraY), 10, HEIGHT - 80);
//			game.customFont.draw(game.batchInterface, "health: " + ((ShipData)ship.body.getUserData()).health, 10, HEIGHT - 110);
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
    for (int i = 0; i < 370; i++) {
      stars.add(new Star(MathUtils.random(0.93f, 0.98f), 0.36f));
    }
    for (int i = 0; i < 120; i++) {
      stars.add(new Star(MathUtils.random(0.87f, 0.92f), 0.51f));
    }
    for (int i = 0; i < 45; i++) {
      stars.add(new Star(MathUtils.random(0.81f, 0.85f), 0.67f));
    }
  }

  @Override
  public void resize(int width, int height) {
    if (width * height == 0) return;
    float resizeCoefficient = height / (float) HEIGHT;
    WIDTH = width; HEIGHT = height;
    SCREEN_WIDTH = (float) WIDTH / (float) HEIGHT * SCREEN_HEIGHT;
    cameraScreenCoefficient = HEIGHT / SCREEN_HEIGHT;
    camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);
    game.cameraInterface.setToOrtho(false, width, height);
    game.batchInterface.setProjectionMatrix(game.cameraInterface.combined);
    joystick.handleResize(resizeCoefficient);
    shooter.handleResize(resizeCoefficient);
    radar.handleResize(resizeCoefficient);
    healthIndicator.resize(height);
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
    joystick.dispose();
    radar.dispose();
    got.dispose();
  }

  @Override
  public void show() {
    System.out.println("show method called");
    Gdx.input.setInputProcessor(new GestureDetector(new GameController(ship.definition.maxZoom)));
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
