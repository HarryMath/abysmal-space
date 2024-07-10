package com.mikilangelo.abysmal.screens.game.actors.fixtures;

import com.mikilangelo.abysmal.shared.repositories.AsteroidsRepository;
import com.mikilangelo.abysmal.shared.repositories.ExplosionsRepository;
import com.mikilangelo.abysmal.shared.repositories.SoundsRepository;
import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;
import com.mikilangelo.abysmal.screens.game.actors.decor.animations.Explosion;
import com.mikilangelo.abysmal.screens.game.actors.basic.DynamicObject;
import com.mikilangelo.abysmal.screens.game.objectsData.AsteroidData;
import com.mikilangelo.abysmal.shared.tools.BodLLoader;
import com.mikilangelo.abysmal.shared.tools.CalculateUtils;
import com.mikilangelo.abysmal.screens.game.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Array;
import com.mikilangelo.abysmal.shared.tools.Random;

public class Asteroid implements DynamicObject {

  private static final float healthWeight = 11.7f;
  private static Sound explosionSound;
  private static final float[] bodyScales = new float[] {
          0.42f, 0.33f, 0.41f, 0.51f, 0.62f,
          1.55f,
          2.30f, 3.51f, 2.40f, 3.9f, 3, 3, 3.88f
  };
  private static final float[] sqrtmass = new float[bodyScales.length];
  private static final float textureScale = 0.0021f;
  public static final byte bigAmount = 7;
  public static final byte mediumAmount = 1;
  public static final byte smallAmount = 5;
  public static final int typesAmount = bigAmount + smallAmount + mediumAmount;

  private static Array<Sprite> textures;
  private static Array<BodLLoader> bodies;
  private static final Array<Sprite> explosion = new Array<>();
  private static final Array<Sprite> stoneExplosion = new Array<>();
  private static boolean initialized = false;


  public int asteroidTypeId;
  private boolean bodyLoaded = false;
  public boolean destroyed = false;
  public Body body;
  private final AsteroidData bodyData;
  public final long asteroidId;
  private final Random random;
  private Vector2 position;
  public float x, y;

  private boolean isExplodedRemotely = false;
  private float deathX, deathY, deathAngle;

  public Asteroid(long asteroidId, int typeId, float x, float y) {
    this.asteroidTypeId = typeId;
    this.asteroidId = asteroidId;
    this.random = new Random(asteroidId);
    bodyData = loadBody(x, y);
    if (sqrtmass[asteroidTypeId] == 0) {
      sqrtmass[asteroidTypeId] = (float) Math.sqrt(body.getMass());
    }
  }

  public float getSize() {
    return bodyScales[asteroidTypeId];
  }

  public static void init() {
    if (!initialized) {
      textures = new Array<>();
      bodies = new Array<>();
      loadBodies(smallAmount, "small");
      loadBodies(mediumAmount, "medium");
      loadBodies(bigAmount, "big");
      for (byte i = 0; i < 6; i++) {
        Sprite s = new Sprite(TexturesRepository.get("explosions/small/" + i + ".png"));
        s.setScale( GameScreen.SCREEN_HEIGHT * textureScale * 0.41f);
        explosion.add(s);
      }
      for (byte i = 0; i < 4; i++) {
        Sprite s = new Sprite(TexturesRepository.get("explosions/stone/" + i + ".png"));
        s.setScale( GameScreen.SCREEN_HEIGHT * textureScale * 1.1f);
        stoneExplosion.add(s);
      }
      explosionSound = SoundsRepository.getSound("explosions/stone/sound.mp3");
      initialized = true;
    }
  }

  public float getSqrtMass() {
    return sqrtmass[asteroidTypeId];
  }

  private static void loadBodies(byte amount, String type) {
    for (byte i = 0; i < amount; i++) {
      Sprite s = new Sprite(TexturesRepository.get("asteroids/" + type + "/" + i + "/texture.png"));
      s.setScale( GameScreen.SCREEN_HEIGHT * textureScale);
      textures.add(s);
      bodies.add(new BodLLoader(Gdx.files.internal("asteroids/" + type + "/" + i + "/body.json")));
    }
  }

  public AsteroidData loadBody(float x, float y) {
    if (!initialized) {
      return null;
    }
    this.x = x; this.y = y;
    this.position = new Vector2(x, y);
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.DynamicBody;
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.density = 1.1f;
    fixtureDef.friction = 0.7f;
    fixtureDef.restitution = 0.5f;
    body = GameScreen.world.createBody(bodyDef);
    bodies.get(asteroidTypeId).attachFixture(body, "Name", fixtureDef, bodyScales[asteroidTypeId]);
    float ang = random.nextFloat(0, 6.28f);
    body.setTransform(x, y, ang);
    body.setAwake(false);
    AsteroidData asteroidData = new AsteroidData();
    asteroidData.health = healthWeight * (float) Math.pow(body.getMass(), 0.6f) * random.nextFloat(1.1f, 3f);
    body.setUserData(asteroidData);
    bodyLoaded = true;
    return asteroidData;
  }

  public void setExploded(float x, float y, float angle) {
    isExplodedRemotely = true;
    deathX = x;
    deathY = y;
    deathAngle = angle;
    bodyData.health = -1;
  }

  public void destroyBody() {
    GameScreen.world.destroyBody(this.body);
    this.body = null;
  }

  @Override
  public void move(float delta) {
    if (this.bodyLoaded && !destroyed) {
      position = body.getPosition();
      x = position.x;
      y = position.y;
      final Vector2 speed = body.getLinearVelocity();
      body.setAngularVelocity(body.getAngularVelocity() * 0.99f);
      body.setLinearVelocity(speed.scl(0.997f));
      if (bodyData.health <= 0) {
        destroyed = true;
        if (isExplodedRemotely) {
          x = (deathX + x) * 0.5f;
          y = (deathY + y) * 0.5f;
          body.setTransform(x, y, deathAngle);
        } else {
          GameScreen.enemiesProcessor.explodeAsteroid(asteroidId, x, y, body.getAngle());
        }
        if (asteroidTypeId >= smallAmount) {
          final float distance = CalculateUtils.distance(x, y, GameScreen.camera.X, GameScreen.camera.Y);
          if (distance < 50) {
            explosionSound.play(1 - distance / 50);
          }
          final int amount = 4 + random.nextInt(1, 4);
          for (byte i = 0; i < amount; i++) {
            final float xSkew = random.nextFloat(-0.7f, 0.7f);
            final float ySkew = random.nextFloat(-0.7f, 0.7f);
            final int type = random.nextInt(0, smallAmount - 1);
            Asteroid newAsteroid = new Asteroid(random.getSeed(), type, x + xSkew, y + ySkew);
            newAsteroid.body.applyAngularImpulse(random.nextFloat(-0.9f, 0.9f), true);
            newAsteroid.body.applyLinearImpulse(
                    (random.nextFloat(-0.05f, 0.05f) + speed.x * 0.1f + xSkew * 1.5f),
                    (random.nextFloat(-0.05f, 0.05f) + speed.y * 0.1f + ySkew * 1.5f),
                    x + xSkew, y + ySkew, true);
            AsteroidsRepository.add(newAsteroid);
          }
        }
        ExplosionsRepository.addExplosion(new Explosion(stoneExplosion, x, y, 0.05f));
      }
    }
  }

  @Override
  public void draw(Batch batch) {
    if (!destroyed) {
      textures.get(asteroidTypeId).setRotation(this.body.getAngle() * MathUtils.radiansToDegrees);
      textures.get(asteroidTypeId).setCenter(this.body.getPosition().x, this.body.getPosition().y);
      textures.get(asteroidTypeId).draw(batch);
    }
  }

  public static void dispose() {
    textures.clear();
    bodies.clear();
    stoneExplosion.clear();
    explosion.clear();
    initialized = false;
  }
}
